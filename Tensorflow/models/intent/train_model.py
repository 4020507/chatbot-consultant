import pandas as pd
import tensorflow as tf
from tensorflow.keras import preprocessing
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input, Embedding, Dense, Dropout, Conv1D, GlobalMaxPool1D, concatenate
import os

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
#데이터 읽기
#query와 intent 데이터를 각각 리스트에 저장한다.
train_file = "train_data_counsel.csv"
data = pd.read_csv(train_file, delimiter=',')
queries = data['query'].tolist()
intents = data['intent'].tolist()

from utils.Preprocess import Preprocess
p = Preprocess(word2index_dic='../../train_tools/dict/chatbot_dict.bin',userdic='../../utils/user_dic.tsv')

#단어 시퀀스 생성
sequences = []
for sentence in queries:
    pos = p.pos(sentence)
    keywords = p.get_keywords(pos, without_tag=True)
    seq = p.get_wordidx_sequence(keywords)
    sequences.append(seq)

#단어 인덱스 스퀸스 벡터 생성, 패딩 처리 하여 동일한 벡터 크기를 맞춘다. padding = post란 뒷쪽으로 패딩한다는 뜻,pre는 앞쪽
from config.GlobalParams import MAX_SEQ_LEN
padded_seqs = preprocessing.sequence.pad_sequences(sequences, maxlen=MAX_SEQ_LEN, padding='post')

# (105658, 15)
print(padded_seqs.shape)
print(len(intents)) #105658

#학습:검증:테스트 = 7:2:1
ds = tf.data.Dataset.from_tensor_slices((padded_seqs, intents))
ds = ds.shuffle(len(queries))

train_size = int(len(padded_seqs) * 0.7)
val_size = int(len(padded_seqs) * 0.2)
test_size = int(len(padded_seqs) * 0.1)
#tkae-> val_size 만큼 가지고 오겠다는 뜻, skip(size) size만큼 스킵 하겠다는 뜻
train_ds = ds.take(train_size).batch(20)
val_ds = ds.skip(train_size).take(val_size).batch(20)
test_ds = ds.skip(train_size + val_size).take(test_size).batch(20)

# 하이퍼 파라미터 설정
dropout_prob = 0.5
EMB_SIZE = 128 #밀집 벡터의 크기
EPOCH = 100
VOCAB_SIZE = len(p.word_index) + 1 #전체 단어 개수

#CNN, 케라스 함수형 모델 방식
#단어 임베딩
input_layer = Input(shape=(MAX_SEQ_LEN,))
embedding_layer = Embedding(VOCAB_SIZE, EMB_SIZE, input_length=MAX_SEQ_LEN)(input_layer)
dropout_emb = Dropout(rate=dropout_prob)(embedding_layer)

#특징 추출
conv1 = Conv1D(
    filters=128,
    kernel_size=3,
    padding='valid',
    activation=tf.nn.relu)(dropout_emb)
pool1 = GlobalMaxPool1D()(conv1)

conv2 = Conv1D(
    filters=128,
    kernel_size=4,
    padding='valid',
    activation=tf.nn.relu)(dropout_emb)
pool2 = GlobalMaxPool1D()(conv2)

conv3 = Conv1D(
    filters=128,
    kernel_size=5,
    padding='valid',
    activation=tf.nn.relu)(dropout_emb)
pool3 = GlobalMaxPool1D()(conv3)

#세 개의 계층 중 가장 큰 점수를 가진 노드가 예측 값이 된다.
concat = concatenate([pool1, pool2, pool3])

#분류
#logits은 softmax나 sigmoid 연산을 수행하기 전에 함수 내부에서 안전하게 계산하기 위해 사용이 된다. tensorflow만의 특이한 점인 듯
hidden = Dense(128, activation=tf.nn.relu)(concat)
dropout_hidden = Dropout(rate=dropout_prob)(hidden)
logits = Dense(217, name='logits')(dropout_hidden)
predictions = Dense(217, activation=tf.nn.softmax)(logits) #217개의 의도 분류이므로 217로 설정

#정의한 계층들을 케라스 모델에 추가
#adam은 stepsize가 gradient의 rescaling에 영향을 받지 않아 안정적인 최적화가 가능하다고 한다. stepsize를 과거의 gradient를 참고하여 adapted 시킬수 있다고 함.
#https://dalpo0814.tistory.com/29
#3개 이상의 선택지 중 하나를 골라야 하는 문제라면 sparse_categorical_crossentroy를 사용한다.
model = Model(inputs=input_layer, outputs=predictions)
model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

#학습 시작, 학습용 데이터와 검증용 데이터 셋을 입력
model.fit(train_ds,validation_data=val_ds,epochs=EPOCH,verbose=1)

loss, accuracy = model.evaluate(test_ds, verbose=1)
print('Accuracy: %f' % (accuracy * 100))
print('loss: %f' % (loss))


# 모델 저장  ○8
model.save('intent_model.h5')

# Convert the model.
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model.
with open('model.tflite', 'wb') as f:
  f.write(tflite_model)
#2021.03.03
#Accuracy: 99.659252
#loss: 0.009341