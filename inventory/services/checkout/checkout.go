package checkout

import (
	"fmt"
	"io"
	"log"
	"net/http"

	"github.com/opentracing/opentracing-go"
	otrext "github.com/opentracing/opentracing-go/ext"

	"github.com/go-chi/chi"

	. "wavefront.com/polyglot/inventory/internal"
)

type CheckoutServer struct {
	HostURL string
	Router  *chi.Mux
	tracer opentracing.Tracer
}

func NewServer() Server {
	r := chi.NewRouter()
	server := &CheckoutServer{GlobalConfig.CheckoutHost, r, opentracing.GlobalTracer()}
	r.Route("/inventory", func(r chi.Router) {
		r.Post("/checkout/{orderId}", server.doCheckout)
	})
	return server
}

func (s *CheckoutServer) Start() error {
	log.Printf("Checkout Server listening: %s", s.HostURL)
	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *CheckoutServer) doCheckout(w http.ResponseWriter, r *http.Request) {
	span := s.tracer.StartSpan("checkout")
	defer span.Finish()

	RandSimDelay()

	if RAND.Float32() < GlobalConfig.SimFailCheckout {
		otrext.Error.Set(span, true)
		WriteError(w, "checkout failure", http.StatusServiceUnavailable)
		return
	}

	resp, err := callWarehouse(span.Context())
	if err != nil {
		otrext.Error.Set(span, true)
		WriteError(w, err.Error(), http.StatusPreconditionFailed)
		return
	}
	defer resp.Body.Close()

	RandSimDelay()

	if resp.StatusCode == http.StatusOK {
		io.Copy(w, resp.Body)
	} else {
		otrext.Error.Set(span, true)
		WriteError(w, fmt.Sprintf("failed to checkout: %s", resp.Status), resp.StatusCode)
	}
}

func callWarehouse(spanCtx opentracing.SpanContext) (*http.Response, error) {
	getURL := fmt.Sprintf("http://%s/warehouse/%s", GlobalConfig.WarehouseHost, "32jf")
	return GETCall(getURL, nil, spanCtx)
}
