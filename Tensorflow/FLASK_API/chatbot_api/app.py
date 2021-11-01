from flask import Flask, request, jsonify, abort
import socket
import json

# 챗봇 엔진 서버 접속 정보
host = "127.0.0.1"  # 챗봇 엔진 서버 IP 주소
port = 5050  # 챗봇 엔진 서버 통신 포트

# Flask 어플리케이션
app = Flask(__name__)


# 챗봇 엔진 서버와 통신, 챗봇 엔진 서버에 소켓 통신으로 질의 전송
# 답변 데이터를 성공적으로 수신한 경우 응답으로 받은 JSON 문자열을 딕셔너리 객체로 변환
#/test/chatbot_client_test.py 주석 확인
def get_answer_from_engine(bottype, query):
    # 챗봇 엔진 서버 연결
    mySocket = socket.socket()
    mySocket.connect((host, port))

    # 챗봇 엔진 질의 요청
    json_data = {
        'Query': query,
        'BotType': bottype
    }
    message = json.dumps(json_data)
    mySocket.send(message.encode())

    # 챗봇 엔진 답변 출력
    data = mySocket.recv(2048).decode()
    ret_data = json.loads(data)

    # 챗봇 엔진 서버 연결 소켓 닫기
    mySocket.close()

    return ret_data


@app.route('/', methods=['GET'])
def index():
    print('hello')

# 챗봇 엔진 query 전송 API
@app.route('/query/<bot_type>', methods=['POST'])
def query(bot_type):
    body = request.get_json()
    print(bot_type)
    try:
        if bot_type == 'TEST': #flask 구동 시
            # 챗봇 API 테스트
            ret = get_answer_from_engine(bottype=bot_type, query=body['query'])
            return jsonify(ret)

        elif bot_type == "KAKAO":
            # 카카오톡 스킬 처리
            body = request.get_json() #스킬 페이로드 불러오기
            utterance = body['userRequest']['utterance'] #사용자 발화만 추출
            ret = get_answer_from_engine(bottype=bot_type, query=utterance) #응답 데이터만 받아오기

            from KaKaoTemplate import KakaoTemplate
            skillTemplate = KakaoTemplate()
            return skillTemplate.send_resp(ret)

        elif bot_type == "NAVER":
            # 네이버톡톡 Web hook 처리
            body = request.get_json() #사용자 식별 값과 이벤트 종류를 추출
            user_key = body['user']
            event = body['event']

            from NaverEvent import NaverEvent
            authorization_key = '' #인증키 입력
            naverEvent = NaverEvent(authorization_key)

            if event == "open":
                #사용자가 채팅방에 들어왔을 때
                print("채팅방에 유저가 들어왔습니다.")
                return json.dumps({}), 200
            elif event == "leave":
                print("유저가 나갔습니다.")
                return json.dumps({}),200
            elif event == "send":
                user_text = body['textContent']['text']
                ret = get_answer_from_engine(bottype=bot_type,query=user_text)
                return naverEvent.send_response(user_key,ret)
        else:
            # 정의되지 않은 bot type인 경우 404 오류
            abort(404)

    except Exception as ex:
        # 오류 발생시 500 오류
        abort(500)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

#챗봇 엔진 서버 5050 TCP/IP 소켓 사용자 질의를 해석한 후 적절한 답변을 응답
#챗봇 API 서버 5000 HTTP 챗봇 엔진과 카카오톡 사이를 연결, 스킬서버
#챗봇 엔진 서버와 챗봇 API 서버를 동일한 서버에서 구동시켜야 함, 챗봇 엔진서버란 Bot.py를 의미하는 듯.
#port 및 host를 잘 확인하여 구동시켜 보자.
