import jieba
import jieba.posseg as pseg
import multiprocessing
import platform

jieba.enable_paddle() #启动paddle模式。 0.40版之后开始支持，早期版本不支持
if platform.system() == "Linux":
    jieba.enable_parallel(multiprocessing.cpu_count())

words = pseg.cut("喜子哥？那是谁家的小厮啊，傻的吗？这么热的天，在池塘边做什么？哪个是李世信家属？", use_paddle=True) #paddle模式

for word, flag in words:
    print('%s %s' % (word, flag))