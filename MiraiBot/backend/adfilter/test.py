# -*- coding: UTF-8 -*-

from adfilter.model import AdPredictor
import os

text1 = "考研 四六级  二级  教资 单招 会计 专升本 计算机二级  word  ppt 学校的学习墙"
text2 = "java垃圾！C垃圾！计算机二级垃圾！PS学姐biss！资料墙biss！👴精通CAD!！"

predictor = AdPredictor.from_saved_model()
print(os.getcwd())
print(predictor.predict_ad(text1), predictor.predict_ad(text2))

