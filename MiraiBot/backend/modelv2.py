import torch
from torch import nn

from tqdm import tqdm
import numpy as np
import matplotlib.pylab as plt
import torchvision.models as tvmodels
from torchvision import datasets, transforms
from efficientnet_pytorch import EfficientNet
import os
from dataset import CaptchaDataset
from dataset import getCaptchaDataset


class CaptchaNN(nn.Module):
    @staticmethod
    def version():
        return 2

    def __init__(self):
        super(CaptchaNN, self).__init__()
        self.kernel_size = 5
        self.class_num = 26

        self.eff_model = EfficientNet.from_pretrained('efficientnet-b2')
        self.output_num = 1000

        self.fc1 = nn.Linear(self.output_num, self.class_num)
        self.fc2 = nn.Linear(self.output_num, self.class_num)
        self.fc3 = nn.Linear(self.output_num, self.class_num)
        self.fc4 = nn.Linear(self.output_num, self.class_num)

    def forward(self, x):
        x = self.eff_model(x)
        #print(x.shape)
        x1 = self.fc1(x)
        x2 = self.fc2(x)
        x3 = self.fc3(x)
        x4 = self.fc4(x)
        return x1, x2, x3, x4

    def predict(self, X):
        y1, y2, y3, y4 = self(X)

        _, y1_pred = torch.max(y1.data, dim=1)
        _, y2_pred = torch.max(y2.data, dim=1)
        _, y3_pred = torch.max(y3.data, dim=1)
        _, y4_pred = torch.max(y4.data, dim=1)

        return (y1_pred.item(), y2_pred.item(), y3_pred.item(), y4_pred.item())