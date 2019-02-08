from rest_framework.response import Response
from rest_framework.decorators import api_view
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@api_view(http_method_names=["GET"])
def fetch(request, order_num):
    if not order_num:
        msg = "Invalid Order Num!"
        logging.warning(msg)
        return Response(msg, status=400)
    return Response(
        data={"status": "Order:" + order_num + " fetched from warehouse"},
        status=200)
