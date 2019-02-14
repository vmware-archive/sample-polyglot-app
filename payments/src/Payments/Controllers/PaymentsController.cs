using System;
using System.Threading;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Payments.Models;

namespace Payments.Controllers
{
    [ApiController]
    public class PaymentsController : Controller
    {
        private readonly Random rand = new Random();

        // POST pay
        [Route("pay/{orderNum}")]
        [HttpPost]
        public IActionResult Pay(string orderNum, Payment payment)
        {
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

            try
            {
                if (ProcessPayment(orderNum, payment))
                {
                    return Ok("success");
                }
                else
                {
                    return Ok("fail");
                }
            }
            catch (Exception e)
            {
                return StatusCode(StatusCodes.Status500InternalServerError, e.Message);
            }
        }

        private bool ProcessPayment(string orderNum, Payment payment)
        {
            double processDuration = Math.Max(RandomGauss(500, 150), 200);
            Thread.Sleep(TimeSpan.FromMilliseconds(processDuration));
            if (processDuration > 800)
            {
                throw new TimeoutException("payment timed out");
            }

            double processResult = rand.NextDouble();
            if (processResult < 0.1)
            {
                throw new SystemException("payment server error");
            } else if (processResult < 0.25)
            {
                return false;
            }
            else
            {
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