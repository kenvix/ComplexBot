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
from modelv2 import CaptchaNN


def main():
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    batchSize = 10
    lr = 0.0001
    epochs = 15
    model_path = "captcha-breaker-v%d.pth" % CaptchaNN.version()
    data_path = "./datav2"

    trainIter, testIter = getCaptchaDataset(batchSize, data_path, 224, 224)
    trainNum = len(trainIter) #训练集所有样本数量。用于显示进度条
    testNum = len(testIter) #训练集所有样本数量。用于显示进度条
    reportNum = 8000 #每迭代1000次报告一次学习状况

    net = CaptchaNN()

    net = net.to(device)

    loss_list = []

    loss_fn = nn.CrossEntropyLoss()
    #optimizer = torch.optim.Adam(net.parameters(), lr=lr, weight_decay=0.00000, betas=(0.9, 0.999), eps=1e-08)
    optimizer = torch.optim.Adam(net.parameters(), lr=lr)

    if os.path.exists(model_path):
        print("Loaded saved dict ", model_path)
        net.load_state_dict(torch.load(model_path))

    #torch.save(net, "full-" + model_path)

    for epoch in range(epochs):
        for i, (X, label) in tqdm(enumerate(trainIter), total=trainNum):
            X = X.to(device)
            label = label.to(device)
            label = label.long()

            label1 = label[:, 0]
            label2 = label[:, 1]
            label3 = label[:, 2]
            label4 = label[:, 3]
            # print(CaptchaDataset.decode_label((label1.data, label2.data, label3.data, label4.data)))

            y1, y2, y3, y4 = net(X)

            loss1 = loss_fn(y1, label1)
            loss2 = loss_fn(y2, label2)
            loss3 = loss_fn(y3, label3)
            loss4 = loss_fn(y4, label4)

            optimizer.zero_grad()

            loss = loss1 + loss2 + loss3 + loss4

            loss.backward() #反向传播
            optimizer.step() #执行优化
            loss_list.append(loss.cpu().item())

            if i % 100 == 0:
                torch.save(net.state_dict(), model_path)

            if i % reportNum == 0:
                #report train

                print("> epoch:", epoch)
                _, y1_pred = torch.max(y1.data, dim=1)
                _, y2_pred = torch.max(y2.data, dim=1)
                _, y3_pred = torch.max(y3.data, dim=1)
                _, y4_pred = torch.max(y4.data, dim=1)
                #print(label1.shape[0])
                correct1 = float(torch.sum(y1_pred == label1.data).float() / label1.shape[0])
                correct2 = float(torch.sum(y2_pred == label2.data).float() / label2.shape[0])
                correct3 = float(torch.sum(y3_pred == label3.data).float() / label3.shape[0])
                correct4 = float(torch.sum(y4_pred == label4.data).float() / label4.shape[0])
                correct = np.mean((correct1, correct2, correct3, correct4))

                print("train acc: ", float(correct),  "train loss:", float(loss))

                # report test
                net.eval()
                tacc_list = []
                tloss_list = []

                for j, (tX, tlabel) in tqdm(enumerate(testIter), total=testNum):
                    if j > 100:
                        break
                    tX = tX.to(device)
                    tlabel = tlabel.to(device)
                    tlabel = tlabel.long()

                    tlabel1 = tlabel[:, 0]
                    tlabel2 = tlabel[:, 1]
                    tlabel3 = tlabel[:, 2]
                    tlabel4 = tlabel[:, 3]

                    ty1, ty2, ty3, ty4 = net(tX)

                    tloss1 = loss_fn(ty1, tlabel1)
                    tloss2 = loss_fn(ty2, tlabel2)
                    tloss3 = loss_fn(ty3, tlabel3)
                    tloss4 = loss_fn(ty4, tlabel4)
                    tloss = tloss1 + tloss2 + tloss3 + tloss4

                    _, ty1_pred = torch.max(ty1.data, dim=1)
                    _, ty2_pred = torch.max(ty2.data, dim=1)
                    _, ty3_pred = torch.max(ty3.data, dim=1)
                    _, ty4_pred = torch.max(ty4.data, dim=1)

                    tcorrect1 = float(torch.sum(ty1_pred == tlabel1.data).float() / tlabel1.shape[0])
                    tcorrect2 = float(torch.sum(ty2_pred == tlabel2.data).float() / tlabel2.shape[0])
                    tcorrect3 = float(torch.sum(ty3_pred == tlabel3.data).float() / tlabel3.shape[0])
                    tcorrect4 = float(torch.sum(ty4_pred == tlabel4.data).float() / tlabel4.shape[0])
                    tcorrect = np.mean((tcorrect1, tcorrect2, tcorrect3, tcorrect4))

                    tacc_list.append(float(tcorrect))
                    tloss_list.append(float(tloss))
                    pass


                print("test acc: ", float(torch.mean(torch.Tensor(tacc_list))), "test loss:",
                      float(torch.mean(torch.Tensor(tloss_list))))


                plt.plot(loss_list)
                plt.show()
            pass




if __name__ == '__main__':
    main()
