from utils.Preprocess import Preprocess

sent = "예전보다 화내는 게 과격해진 거 같아."

p = Preprocess(userdic='../utils/user_dic.tsv')

pos = p.pos(sent)
print(pos)

#품사 태그와 같이 키워드 출력
ret = p.get_keywords(pos,without_tag=False)
print(ret)

#품사 태그 없이 키워드 출력
ret = p.get_keywords(pos,without_tag=True)
print(ret)