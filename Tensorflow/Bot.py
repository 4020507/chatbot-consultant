import threading
import json

from config.DatabaseConfig import *
from utils.Database import Database
from utils.BotServer import BotServer
from utils.Preprocess import Preprocess
from models.intent.IntentModel import IntentModel
from models.ner.NerModel import NerModel
from utils.FindAnswer import FindAnswer


# 전처리 객체 생성
p = Preprocess(word2index_dic='train_tools/dict/chatbot_dict.bin',
               userdic='utils/user_dic.tsv')

# 의도 파악 모델
intent = IntentModel(model_name='models/intent/intent_model.h5', proprocess=p)

# 개체명 인식 모델
ner = NerModel(model_name='models/ner/ner_model.h5', proprocess=p)

#챗봇 클라이언트의 서버 연결이 수락되는 순간 실행되는 스레드 함수
def to_client(conn, addr, params):
    db = params['db']

    try:
        db.connect()  # 디비 연결, 서버에 접속하면 가장 먼저 하는 일

        # 데이터 수신,conn: 챗봇 클라이언트 소켓 객체, 이 객체를 통해 클라이언트와 데이터를 주고 받음
        read = conn.recv(2048)  # 수신 데이터가 있을 때 까지 블로킹,최대 2048 바이트만큼 데이터를 수신
        print('===========================')
        print('Connection from: %s' % str(addr))

        if read is None or not read:
            # 클라이언트 연결이 끊어지거나, 오류가 있는 경우
            print('클라이언트 연결 끊어짐')
            exit(0)


        # 수신된 데이터를 json 데이터로 변환, 서버쪽으로 요청하는 JSON 프로토콜
        recv_json_data = json.loads(read.decode())
        print("데이터 수신 : ", recv_json_data)
        query = recv_json_data['Query']

        # 의도 파악
        intent_predict = intent.predict_class(query)
        intent_name = intent.labels[intent_predict]

        # 개체명 파악
        ner_predicts = ner.predict(query)
        ner_tags = ner.predict_tags(query)


        # 답변 검색
        try:
            f = FindAnswer(db)
            answer_text, answer_image = f.search(intent_name, ner_tags)
            answer = f.tag_to_word(ner_predicts, answer_text)

        except:
            answer = "죄송해요 무슨 말인지 모르겠어요. 조금 더 공부 할게요."
            answer_image = None
        #소켓 통신으로는 객체 형태로 데이터 송신이 불가능하므로 json.dumps()함수를 통해 JSON 객체를 문자열로 변환
        #문자열을 utf-8로 인코딩해 챗봇 클라이언트 쪽으로 문자열 데이터를 전송, 전송이 완료되면 연결을 끊은 뒤 스레드 함수의 실행 종료
        send_json_data_str = {
            "Query" : query,
            "Answer": answer,
            "AnswerImageUrl" : answer_image,
            "Intent": intent_name,
            "NER": str(ner_predicts)
        }
        message = json.dumps(send_json_data_str)
        conn.send(message.encode())

    except Exception as ex:
        print(ex)

    finally:
        if db is not None: # db 연결 끊기
            db.close()
        conn.close()


if __name__ == '__main__':

    # 질문/답변 학습 디비 연결 객체 생성
    db = Database(
        host=DB_HOST, user=DB_USER, password=DB_PASSWORD, db_name=DB_NAME
    )
    print("DB 접속")

    #챗봇 서버 소켓 생성, 통신 포트는 5050, 클라이언트 최대 연결 수는 100
    #서버 IP와 서버에서 설정한 통신 포트가 오픈이 되어 있어야 함
    port = 5050
    listen = 100

    # 봇 서버 동작
    bot = BotServer(port, listen)
    bot.create_sock()
    print("bot start")

    #챗봇 클라이언트 연결을 기다리다 클라이언트에서 서버 연결 요청이 들어오면 서버에서 수락되는 즉시 챗봇 클라이언트의 서비스 요청을 처리할 수 있는 스레드 생성
    #생성되는 스레드는 to_client()함수 호출
    while True:
        conn, addr = bot.ready_for_client()
        params = {
            "db": db
        }

        client = threading.Thread(target=to_client, args=(
            conn,
            addr,
            params
        ))
        client.start()