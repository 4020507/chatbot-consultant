from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello():
    return 'Hello Flask'

@app.route('/info/<name>') #URI와 연결된 뷰 함수, <name>은 URI에서 사용되는 동적 변수, 여기에 인자를 입력하여 https://127.0.0.1/info/<name>으로 접속 가능
def get_name(name):
    return "hello {}".format(name)

@app.route('/user/<int:id>') #URI와 연결된 뷰 함수. 데이터 타입이 int로 지정이 되었다. 그러므로 문자열이 아닌 정수로 입력 할 것
def get_user(id):
    return "user id is {}".format(id)
#하나의 뷰 함수에 여러 개의 URI 지정 가능. 2개의 URI를 send_message() 함수에 연결하고 JSON 포맷으로 출력
@app.route('/json/<int:dest_id>/<message>')
@app.route('/JSON/<int:dest_id>/<message>')
def send_message(dest_id, message):
    json = {
    "bot_id": dest_id,
    "message": message
    }
    return json

if __name__ == '__main__':
    app.run()