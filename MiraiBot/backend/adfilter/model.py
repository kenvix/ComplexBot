# -*- coding: UTF-8 -*-

import re

import jieba
import jieba.analyse
import pandas as pd
import joblib
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import MultinomialNB
import numpy as np

class AdPredictor:
    def __init__(self, init=True, stopWordsArray=None):
        self.types = {
            0: "normal",
            1: "pssisterad"
        }

        if init:
            self.vectorizer = TfidfVectorizer(token_pattern=r"(?u)\b\w+\b", binary=True, ngram_range=(1,2))
            self.clf = MultinomialNB(alpha=1, fit_prior=True)

        if stopWordsArray is None:
            stopWordsText = open(r"./data/cn_stopwords.txt", 'r', encoding='utf-8').read() + open(r"./data/hit_stopwords.txt", 'r', encoding='utf-8').read()
            self.stopWordsArray = stopWordsText.split("\n")
        else:
            self.stopWordsArray = stopWordsArray

    def splitWords(self, text):
        allTags = jieba.cut(text, cut_all=True)
        tags = []

        for word in allTags:
            word = re.sub('[a-zA-Z0-9’!"#$%\\n\\r&\'()*+,-./:;<=>?@，。?★、…【】（）《》？“”‘’！[\\]^_`{|}~\s]+', "", word)
            word = re.sub('[\ufff0-\uffff]|[\001\002\003\004\005\006\007\x08\x09\x0a\x0b\x0c\x0d\x0e\x0f\x10\x11\x12\x13\x14\x15\x16\x17\x18\x19\x1a]+', '', word)
            if len(word) >= 1 and word not in self.stopWordsArray:
                tags.append(word.lower())

        return tags

    def transformTextToSparseMatrix(self, texts):
        self.vectorizer.fit(texts) # 生成词汇表
        vocabulary = self.vectorizer.vocabulary_ # 输出词汇表
        vector = self.vectorizer.transform(texts) # 生成向量
        result = pd.DataFrame(vector.toarray())

        keys = []
        values = []
        for key,value in self.vectorizer.vocabulary_.items():
            keys.append(key)
            values.append(value)
        df = pd.DataFrame(data={"key":keys, "value": values})
        colnames = df.sort_values("value")["key"].values

        result.columns = colnames
        return result

    def train(self, textMatrix, y, dir = "./data"):
        X_train, X_test, y_train, y_test = train_test_split(textMatrix, y, test_size=0.10, random_state=100)
        print("Train Num: ", X_train.shape[0], "Test Num: ", X_test.shape[0])
        self.clf = self.clf.fit(X_train, y_train)

        print("Test Acc", self.clf.score(X_test, y_test))
        print("Saving check point ")
        joblib.dump(self.clf, dir + "/clf.pkl")
        joblib.dump(self.vectorizer, dir + "/vectorizer.pkl")

        with open(dir + "/stopWordsArray.pkl", mode='wb') as file:
            pickle.dump(self.stopWordsArray, file)

    @staticmethod
    def from_saved_model(dir="./data"):
        with open(dir + "/stopWordsArray.pkl", mode='rb') as file:
            predictor = AdPredictor(False, stopWordsArray=pickle.load(file))
            predictor.clf = joblib.load(dir + "/clf.pkl")
            predictor.vectorizer = joblib.load(dir + "/vectorizer.pkl")
            return predictor

    def get_type_string(self, type_int) -> str:
        return self.types.get(type_int, None)

    def predict_ad(self, text: str):
        words = self.splitWords(text)
        document = " ".join(words)
        vector = self.vectorizer.transform([document])
        result = pd.DataFrame(vector.toarray())
        predictProba = self.clf.predict_proba(result)
        resultPredicted = np.argmax(predictProba)
        return self.get_type_string(resultPredicted), predictProba[0][resultPredicted]
