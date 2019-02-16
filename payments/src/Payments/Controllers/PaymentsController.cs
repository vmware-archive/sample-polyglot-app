using System;
using System.Threading;
using System.Threading.Tasks;
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

        public PaymentsController(ITracer tracer)
        {
            this.tracer = tracer;
        }

        // POST pay
        [Route("pay/{orderNum}")]
        [HttpPost]
        public IActionResult Pay(string orderNum, Payment payment)
        {
            double duration1 = Math.Max(RandomGauss(100, 25), 50);
            Thread.Sleep(TimeSpan.FromMilliseconds(duration1));

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

            double duration2 = Math.Max(RandomGauss(100, 25), 50);
            Thread.Sleep(TimeSpan.FromMilliseconds(duration2));

            if (duration1 + duration2 > 270)
            {
                throw new TimeoutException("payment timed out");
            }

            if (rand.NextDouble() < 0.1)
            {
                throw new SystemException("payment server error");
            }

            var context = tracer.ActiveSpan.Context;
            Task.Run(async () => await UpdateAccountAsync(context));

            return Accepted("payment accepted");
        }

        private async Task UpdateAccountAsync(ISpanContext context)
        {
            using (var scope = tracer.BuildSpan("UpdateAccountAsync")
                    .AddReference(References.FollowsFrom, context)
                    .StartActive())
            {
                double randDuration = rand.NextDouble() / 3;
                Thread.Sleep(TimeSpan.FromSeconds(1 + randDuration));
                if (rand.NextDouble() < 0.1)
                {
                    scope.Span.SetTag(Tags.Error, true);
                }
                await Task.Delay(0);
            };
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