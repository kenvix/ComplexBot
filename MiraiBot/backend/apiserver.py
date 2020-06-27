import thrift
import json
from predictor import CaptchaPredictor
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer
import socket
import logging
from modelv2 import CaptchaNN
from dataset import CaptchaDataset
from PIL import Image
import torch
logging.basicConfig(level=logging.DEBUG, format='[%(asctime)s][%(levelname)s][%(threadName)-10s] %(message)s')


class APITransmitHandler(CaptchaPredictor.Iface):
    def __init__(self, net: CaptchaNN):
        self.net = net
        self.transform = CaptchaDataset.get_transform(224, 224)
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

    def predict(self, pil_img) -> str:
        img = self.transform(pil_img)
        img = CaptchaDataset.to_var(img)
        X = img.to(self.device)
        pred = CaptchaDataset.decode_label(self.net.predict(X))
        return pred

    def fromFile(self, path) -> str:
        """
        Parameters:
         - path

        """
        logging.debug("Received file request: %s" % path)
        img = Image.open(path)
        predict = self.predict(img)
        return predict

    def fromBinary(self, data) -> str:
        """
        Parameters:
         - data

        """
        return self.predict(Image.frombytes('RGB', (224, 224), data, 'jpeg'))
    pass


class APIServer:
    def __init__(self, net: CaptchaNN, host='127.0.0.1', port=48519):
        handler = APITransmitHandler(net)
        processor = CaptchaPredictor.Processor(handler)
        transport = TSocket.TServerSocket(host, port)
        tfactory = TTransport.TBufferedTransportFactory()
        pfactory = TBinaryProtocol.TBinaryProtocolFactory()

        self.server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)
        pass

    def start(self):
        logging.info("Running API server")
        self.server.serve()

    def get_server(self):
        return self.server


def main():
    logging.info("Loading model")
    model_path = "captcha-breaker-v%d.pth" % CaptchaNN.version()
    net = CaptchaNN()
    net = net.to(torch.device('cuda' if torch.cuda.is_available() else 'cpu'))
    net.load_state_dict(torch.load(model_path))
    net.eval()

    server = APIServer(net)
    server.start()
    pass


if __name__ == '__main__':
    main()