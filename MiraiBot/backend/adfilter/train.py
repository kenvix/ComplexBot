# -*- coding: UTF-8 -*-

from adfilter.model import AdPredictor
import os

import numpy
import pandas as pd

from adfilter.model import AdPredictor


class AdModelTrainer:
    def __init__(self):
        self.dataset = {}
        self.splitWords = {}
        self.document = {}
        self.combined = []
        self.textMatrix = None
        self.predictor = None

    def load_data(self):
        allNormalText = open(r"./data/data-normal.txt", 'r', encoding='utf-8').read()
        self.dataset[0] = allNormalText.split('\n')

        for i in range(1, len(AdPredictor.Types)):
            name = AdPredictor.Types[i]
            allAdText = open(r"./data/data-%s.txt" % name, 'r', encoding='utf-8').read()
            self.dataset[i] = allAdText.replace('\r\n', '\n').split('\n\n')


    def generate_matrix(self):
        self.predictor = AdPredictor()

        for i in range(0, len(AdPredictor.Types)):
            self.splitWords[i] = [list(self.predictor.splitWords(ad)) for ad in self.dataset[i]]
            self.document[i] = [" ".join(sent0) for sent0 in self.splitWords[i]]
            self.combined.extend(self.document[i])

        self.textMatrix = self.predictor.transformTextToSparseMatrix(self.combined)
        return self.textMatrix.head()

    def train_model(self):
        features = pd.DataFrame(self.textMatrix.apply(sum, axis=0))
        # extractedfeatures = [features.index[i] for i in range(features.shape[0]) if features.iloc[i, 0] > 5]

        y = []

        for i in range(0, len(AdPredictor.Types)):
            name = AdPredictor.Types[i]
            document = self.document[i]
            print("Document %s Len: %d" % (name, len(document)))
            y.extend(numpy.full(len(document), i))

        self.predictor.train(self.textMatrix, y)

    def train(self):
        self.load_data()
        self.generate_matrix()
        self.train_model()


def main():
    AdModelTrainer().train()
    pass


if __name__ == '__main__':
    main()