import torch

from captchabreaker.dataset import CaptchaDataset
from captchabreaker.modelv2 import CaptchaNN


class CaptchaBreaker:
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
