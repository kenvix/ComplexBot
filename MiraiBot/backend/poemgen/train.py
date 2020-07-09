# -*- coding: UTF-8 -*-

import os
import re

import pandas as pd
import torch
from torch import nn
from torch.utils import data
from torch.utils.data import DataLoader
import numpy as np


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

        print("dataX[0]", len(self.dataX[0]))
        print("dataX", len(self.dataX))
        print("dataY", len(self.dataY))
        print("word_dataframe", len(word_dataframe))

        X = np.array(self.dataX)
        print("X.shape", X.shape)
        pass

    def __len__(self):
        return len(self.dataX)

    def __getitem__(self, index):
        '''
        :param index:
        :return: data/feature(x), label(y)
        '''

        return torch.from_numpy(np.array(self.dataX[index])), torch.from_numpy(np.array(self.dataY[index]))

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
    def __init__(self, vocab_size, embedding_dim, hidden_dim):
        super(PoemModel, self).__init__()
        self.model = nn.Sequential(
            nn.Embedding(vocab_size, embedding_dim),
            nn.GRU(embedding_dim, hidden_dim),
            nn.Linear(),
            nn.Softmax(),
        )
        pass

    def forward(self, x):
        pass



def main():
    # dataset = PoemDataset.getDataset(64)
    ds = PoemDataset()
    print("len", len(ds))
    print(ds[0])
    print(ds[1])
    pass


if __name__ == '__main__':
    main()
