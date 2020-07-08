# -*- coding: UTF-8 -*-

import os
import re
import pandas as pd
import numpy as np
import torch
from torch.autograd import Variable
from torch.nn.modules.module import T_co
from torch.utils import data
from torch.utils.data import DataLoader
from torchvision import transforms
from ignite.engine import Engine, Events, create_supervised_evaluator
from ignite.metrics import Accuracy
from torch import nn


class PoemDataset(torch.utils.data.Dataset):
    def __init__(self):
        base_path = os.path.split(os.path.realpath(__file__))[0]
        with open(base_path + '/dataset.txt', encoding='utf-8') as f:
            raw_text = f.read()

        poem_text = raw_text.split('\r\n')
        char_list = [re.findall('[\x80-\xff]{3}|[\w\W]', s) for s in poem_text]
        all_words = []
        for i in char_list:
            all_words.extend(i)

        word_dataframe = pd.DataFrame(pd.Series(all_words).value_counts())
        word_dataframe['id'] = list(range(1, len(word_dataframe) + 1))

        word_index_dict = word_dataframe['id'].to_dict()
        index_dict = {}
        for k in word_index_dict:
            index_dict.update({word_index_dict[k]: k})
        seq_len = 3
        self.dataX = []
        self.dataY = []

        for i in range(0, len(all_words) - seq_len, 1):
            seq_in = all_words[i: i + seq_len]
            seq_out = all_words[i + seq_len]
            self.dataX.append([word_index_dict[x] for x in seq_in])
            self.dataY.append(word_index_dict[seq_out])
        pass

    def __len__(self):
        return len(self.dataX)

    def __getitem__(self, index):
        '''
        :param index:
        :return: data/feature(x), label(y)
        '''

        return self.dataX[index], self.dataY[index]

    @staticmethod
    def getDataset(batch_size):
        train_dataset = PoemDataset()
        train_data_loader = DataLoader(train_dataset, batch_size=batch_size, num_workers=0,
                                       shuffle=True, drop_last=True)
        test_data = PoemDataset()
        test_data_loader = DataLoader(test_data, batch_size=batch_size,
                                      num_workers=0, shuffle=True, drop_last=True)
        return train_data_loader, test_data_loader


class PoemModel(nn.Module):
    def __init__(self):
        super(PoemModel, self).__init__()
        self.model = nn.Sequential(
            nn.Embedding(),
            nn.GRU(),
            nn.Linear(),
            nn.Softmax(),
        )
        pass

    def forward(self, x) -> T_co:
        pass



def main():
    dataset = PoemDataset.getDataset(64)
    pass


if __name__ == '__main__':
    main()
