import logging
from platform import python_version
import os
import torch
from PIL import Image
from thrift.server import TNonblockingServer
from thrift.transport import TSocket

from backend import BackendBridge
from captchabreaker.modelv2 import CaptchaNN
from captchabreaker.utils import CaptchaBreaker
from adfilter.model import AdPredictor

logging.basicConfig(level=logging.DEBUG, format='[%(asctime)s][%(levelname)s][%(threadName)-10s] %(message)s')


class APITransmitHandler(BackendBridge.Iface):
    def __init__(self, captchaBreaker: CaptchaBreaker, adPredictor: AdPredictor):
        self.captchaBreaker = captchaBreaker
        self.adPredictor = adPredictor

    def parseCaptchaFromFile(self, path) -> str:
        logging.debug("Received file request: %s" % path)
        img = Image.open(path)
        predict = self.captchaBreaker.predict(img)
        return predict

    def parseCaptchaFromBinary(self, data) -> str:
        return self.captchaBreaker.predict(Image.frombytes('RGB', (224, 224), data, 'jpeg'))

    def classificateTextMessage(self, text):
        return self.adPredictor.predict_ad(text)

    def ping(self, data):
        return data

    def operate(self, operate):
        return "NotImplemented"

    def getAboutInfo(self):
        return "PyBotBackend v0.1 @ Python " + python_version()

    def getBackendVersionCode(self):
        return 1


class APIServer:
    def __init__(self, net: CaptchaBreaker, adPredictor: AdPredictor, host='127.0.0.1', port=48519):
        handler = APITransmitHandler(net, adPredictor)
        processor = BackendBridge.Processor(handler)
        transport = TSocket.TServerSocket(host, port)

        self.server = TNonblockingServer.TNonblockingServer(processor, transport)
        pass

    def start(self):
        logging.info("Running API server")
        self.server.serve()

    def get_server(self):
        return self.server


def main(host='127.0.0.1', port=48519):
    logging.info("Root path %s" % os.getcwd())
    logging.info("Loading model AdFilter ...")
    adPredictor = AdPredictor.from_saved_model("./adfilter/data")

    logging.info("Loading model CaptchaBreaker ...")
    model_path = "captcha-breaker-v%d.pth" % CaptchaNN.version()
    net = CaptchaNN()
    net = net.to(torch.device('cuda' if torch.cuda.is_available() else 'cpu'))
    net.load_state_dict(torch.load(model_path))
    net.eval()
    captchaBreaker = CaptchaBreaker(net)

    logging.info("Loading API Server ...")
    server = APIServer(captchaBreaker, adPredictor, host=host, port=port)
    server.start()

    logging.info("Shutdown system ...")
    pass


if __name__ == '__main__':
    main()
