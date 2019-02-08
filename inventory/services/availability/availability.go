package availability

import (
	"log"
	"net/http"

	"github.com/go-chi/chi"
	"github.com/opentracing/opentracing-go"
	otrext "github.com/opentracing/opentracing-go/ext"

	. "wavefront.com/polyglot/inventory/internal"
)

type AvailabilityServer struct {
	HostURL       string
	Router        *chi.Mux
	tracer opentracing.Tracer
}

func NewServer() Server {
	r := chi.NewRouter()
	server := &AvailabilityServer{GlobalConfig.AvailabilityHost, r, opentracing.GlobalTracer()}
	r.Route("/inventory", func(r chi.Router) {
		r.Get("/available/{itemId}", server.available)
	})
	return server
}

func (s *AvailabilityServer) Start() error {
	log.Printf("Availability Server listening on: %s", s.HostURL)
	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *AvailabilityServer) available(w http.ResponseWriter, r *http.Request) {
	span := NewServerSpan(r, "available")
	defer span.Finish()

	RandSimDelay()

	if RAND.Float32() < GlobalConfig.SimFailDelivery1 {
		otrext.Error.Set(span, true)
		WriteError(w, "Failed to check availability", http.StatusServiceUnavailable)
		return
	}

	exists := true
	if RAND.Float32() < GlobalConfig.SimFailDelivery2 {
		exists = false
	}

	if !exists {
		otrext.Error.Set(span, true)
		WriteError(w, "Item does not exist", http.StatusNotFound)
		return
	}
	w.Write([]byte{byte(http.StatusOK)})
}
