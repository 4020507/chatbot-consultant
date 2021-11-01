from flask import Flask, request, jsonify
app = Flask(__name__)

# 서버 리소스, GET, POST 메서드로 호출된 REST API를 통해 해당 리소스에 객체를 추가하고 불러오도록 구현
resource = []

# 사용자 정보 조회
#user_id에 맞는 사용자 정보를 조회하는 GET 메서드의 REST API를 정의
#GET은 라우트 데커레이터 인자에서 methods를 생략 가능
#get_user 함수를 이용하여 리소스를 탐색해 user_id 값으로 저장된 데이터가 있다면 해당 객체를 JSON으로 응답
@app.route('/user/<int:user_id>', methods=['GET'])
def get_user(user_id):

    for user in resource:
        if user['user_id'] is user_id:
            return jsonify(user)

    return jsonify(None)


# 사용자 추가
#HTTP 요청 시 Body에 포함된 JSON 데이터를 서버 리소스에 추가한 후 현재 저장된 전체 리소스 데이터를 JSON으로 변환해 응답
@app.route('/user', methods=['POST'])
def add_user():
    user = request.get_json() #HTTP 요청 Body의 JSON 객체를 딕셔너리 형태로 가저옴
    resource.append(user)
    return jsonify(resource) #JSON 응답형태로 변환



if __name__ == '__main__':
    app.run()