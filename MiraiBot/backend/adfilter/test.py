# -*- coding: UTF-8 -*-

from adfilter.model import AdPredictor
import os

text1 = "这个是咱们学校的学习墙开学准备（基本包含各学科）技能提升（计算机二级  word  ppt等）各种考证资料（考研 四六级  二级  教资 单招 会计 专升本等）基本都有的，抗疫时期闲着也是闲着 需要的加墙墙就好啦 "
text2 = "java垃圾！C垃圾！计算机二级垃圾！PS学姐biss！资料墙biss！👴精通CAD!！"

predictor = AdPredictor.from_saved_model()
print(os.getcwd())
print(predictor.predict_ad(text1), predictor.predict_ad(text2))

