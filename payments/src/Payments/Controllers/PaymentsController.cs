using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using OpenTracing;
using OpenTracing.Tag;
using Payments.Models;

namespace Payments.Controllers
{
    [ApiController]
    public class PaymentsController : Controller
    {
        private readonly Random rand = new Random();
        private readonly ITracer tracer;
        private readonly HttpClient httpClient;

        public PaymentsController(ITracer tracer)
        {
            this.tracer = tracer;
            this.httpClient = new HttpClient();
        }

        // POST pay
        [Route("pay/{orderNum}")]
        [HttpPost]
        public IActionResult Pay(string orderNum, Payment payment)
        {
            if (rand.NextDouble() < 0.01)
            {
                string url = $"{Request.Scheme}://{Request.Host.ToUriComponent()}/health";
                Task.Run(async () => await httpClient.GetAsync(url));
            }

            Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(40, 10), 20)));

            if (string.IsNullOrWhiteSpace(orderNum))
            {
                return BadRequest("invalid order number");
            }
            else if (string.IsNullOrWhiteSpace(payment.Name))
            {
                return BadRequest("invalid name");
            }
            else if (string.IsNullOrWhiteSpace(payment.CreditCardNum))
            {
                return BadRequest("invalid credit card number");
            }

            var context = tracer.ActiveSpan.Context;
            IActionResult result = rand.NextDouble() < 0.5 ? FastPay(context) : ProcessPayment(context);
            Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(20, 5), 10)));

            Task.Run(async () => await UpdateAccountAsync(context));
            Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(10, 2), 5)));

            return result;
        }

        private IActionResult FastPay(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("FastPay")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(80, 20), 40)));
                if (rand.NextDouble() < 0.001)
                {
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(StatusCodes.Status500InternalServerError, "fast pay failed");
                }
                return Accepted("fast pay accepted");
            };
        }

        private IActionResult ProcessPayment(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("ProcessPayment")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(25, 5), 15)));
                if (rand.NextDouble() < 0.001)
                {
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(StatusCodes.Status500InternalServerError, "payment failed");
                }

                if (!AuthorizePayment(scope.Span.Context))
                {
                    return StatusCode(StatusCodes.Status500InternalServerError, "payment authorization failed");
                }
                Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(15, 3), 10)));
                return FinishPayment(scope.Span.Context);
            };
        }

        private bool AuthorizePayment(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("AuthorizePayment")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(100, 25), 50)));
                if (rand.NextDouble() < 0.001)
                {
                    scope.Span.SetTag(Tags.Error, true);
                    return false;
                }
                return true;
            };
        }

        private IActionResult FinishPayment(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("FinishPayment")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(60, 15), 30)));
                if (rand.NextDouble() < 0.001)
                {
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(StatusCodes.Status500InternalServerError, "payment failed");
                }
                return Accepted("payment accepted");
            };
        }

        private async Task UpdateAccountAsync(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("UpdateAccountAsync")
                    .AddReference(References.FollowsFrom, context)
                    .StartActive())
            {
                double randDuration = rand.NextDouble() / 3;
                await Task.Delay(TimeSpan.FromSeconds(1 + randDuration));
                if (rand.NextDouble() < 0.001)
                {
                    scope.Span.SetTag(Tags.Error, true);
                }
            };
        }

        // GET health
        [Route("health")]
        [HttpGet]
        public async Task<IActionResult> GetHealthAsync()
        {
            Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(20, 5), 10)));
            var context = tracer.ActiveSpan.Context;
            if (await CheckHealthAsync(context))
            {
                return Ok("healthy");
            }
            return StatusCode(StatusCodes.Status503ServiceUnavailable, "unavailable");
        }

        private async Task<bool> CheckHealthAsync(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("CheckHealthAsync")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                Thread.Sleep(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(20, 5), 10)));
                var taskDb = CheckDbHealthAsync(scope.Span.Context);
                var taskAuth = CheckAuthHealthAsync(scope.Span.Context);
                var results = await Task.WhenAll(taskDb, taskAuth);
                foreach (bool healthy in results)
                {
                    if (!healthy)
                    {
                        scope.Span.SetTag(Tags.Error, true);
                        return false;
                    }
                }
                return true;
            }
        }

        private async Task<bool> CheckDbHealthAsync(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("CheckDbHealthAsync")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                await Task.Delay(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(50, 10), 30)));
                if (rand.NextDouble() < 0.01)
                {
                    scope.Span.SetTag(Tags.Error, true);
                    return false;
                }
                return true;
            }
        }

        private async Task<bool> CheckAuthHealthAsync(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("CheckAuthHealthAsync")
                    .AddReference(References.ChildOf, context)
                    .StartActive())
            {
                await Task.Delay(TimeSpan.FromMilliseconds(Math.Max(RandomGauss(45, 10), 25)));
                if (rand.NextDouble() < 0.01)
                {
                    scope.Span.SetTag(Tags.Error, true);
                    return false;
                }
                return true;
            }
        }

        private double RandomGauss(double mean, double stdDev)
        {
            double u1 = 1.0 - rand.NextDouble();
            double u2 = 1.0 - rand.NextDouble();
            double randStdNormal = Math.Sqrt(-2.0 * Math.Log(u1)) *
                         Math.Sin(2.0 * Math.PI * u2);
            double randNormal =
                         mean + stdDev * randStdNormal;
            return randNormal;
        }
    }
}