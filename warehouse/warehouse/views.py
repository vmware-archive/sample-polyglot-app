import logging
import random
import time
import requests
from concurrent.futures import ThreadPoolExecutor
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.conf import settings

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

tracing = settings.OPENTRACING_TRACING
tracer = tracing.tracer
executor = ThreadPoolExecutor(max_workers=2)


@api_view(http_method_names=["GET"])
def fetch(request, order_num):
    time.sleep(1)
    if random.randint(1, 1000) == 1000:
        msg = "Random Service Unavailable!"
        logging.warning(msg)
        return Response(msg, status=503)
    if not order_num:
        msg = "Invalid Order Num!"
        logging.warning(msg)
        return Response(msg, status=400)
    executor.submit(async_fetch, tracer.active_span)
    if random.randint(1, 3) == 3:
        requests.get(
            "http://localhost:" + request.META["SERVER_PORT"] + "/check_stock")
    return Response(
        data={"status": "Order:" + order_num + " fetched from warehouse"},
        status=202)


def async_fetch(parent_span):
    with tracer.scope_manager.activate(parent_span, finish_on_close=True):
        with tracer.start_active_span('async_fetch') as scope:
            time.sleep(2)
            if random.randint(1, 1000) == 1000:
                scope.span.set_tag("error", "true")
            return


@api_view(http_method_names=["GET"])
def check_stock(request):
    time.sleep(1)
    schedule_checking(tracer.active_span)
    return Response(status=202)


def schedule_checking(parent_span):
    with tracer.scope_manager.activate(parent_span, finish_on_close=True):
        with tracer.start_active_span('schedule_checking') as scope:
            time.sleep(1)
            executor.submit(async_check, scope.span)
            return


def async_check(parent_span):
    with tracer.scope_manager.activate(parent_span, finish_on_close=True):
        with tracer.start_active_span('async_check'):
            time.sleep(1)
            return
