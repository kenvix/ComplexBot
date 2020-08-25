import traceback

try:
    1/0
except Exception as e:
    print("Message:", str(e))
    print("Message", type(e).__name__)