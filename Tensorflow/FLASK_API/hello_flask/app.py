from flask import Flask

#flask 클래서 생성자로 현재 실행되는 애플리케이션 모듈명을 전달해야 한다.
#__name__을 사용하면 현재 실행되는 애플리케이션의 모듈명이 자동으로 들어간다.
app = Flask(__name__)

#특정 URI를 호출했을 때 실행되는 함수를 정의, 함수의 결과값이 웹 브라우저 화면에 보이기 때문에 view 함수라고 불림

@app.route('/') #route, URI를 처리하는 함수를 연결할 수 있는 방법 제공, /를 호출했을 때 실행되는 뷰 함수 정의
def hello():
    return 'Hello Flask'

if __name__ == '__main__':
    app.run() #app.run(host='127.0.0.1', post='5000') 이런식으로 설정가능