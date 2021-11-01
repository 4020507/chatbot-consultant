from utils.Preprocess import Preprocess
from tensorflow.keras import preprocessing
import pickle

#말뭉치 데이터 읽어오기
#tab을 사용하여 id, document, label 컬럼
#사용한 단어가 존재하지 않을 경우 OOV로 처리, corpus.txt에서 단어를 새로 업데이트해주는게 좋음

def read_corpus_data(filename):
    with open(filename, 'r', encoding='UTF-8') as f:
        data = [line.split('\t') for line in f.read().splitlines()]
        data = data[1:]
    return data

corpus_data = read_corpus_data('./corpus.txt')

#POS를 태깅하여 단어 리스트 dict에 저장
p = Preprocess()
dict = []
for c in corpus_data:
    #c[0]: corpus.txt 참조, 가장 첫번째 있는 숫자
    #c[1]: 문장, 고량주 주문 관련 문의 드립니다.
    #c[2]: 번호
    pos = p.pos(c[1])
    for k in pos:
        #k[0]: 단어, 고량주, 주문, 관련...
        #k[1]: 품사 태깅 NNP,NNG...
        dict.append(k[0])

#단어 리스트를 word_index 데이터로 만든다.
tokenizer = preprocessing.text.Tokenizer(oov_token='OOV')
tokenizer.fit_on_texts(dict)
word_index = tokenizer.word_index
count = 1
for k in word_index:
    print(k,count)
    count = count+1

#word_index를 파일로 저장

f = open("chatbot_dict.bin","wb")
try:
    pickle.dump(word_index,f)
except Exception as e:
    print(e)
finally:
    f.close()