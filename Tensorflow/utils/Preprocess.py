from konlpy.tag import Komoran
import pickle

class Preprocess:
    def __init__(self, word2index_dic='', userdic=None):
        self.komoran = Komoran(userdic=userdic)

        # 단어 인덱스 사전 불러오기
        if (word2index_dic != ''):
            f = open(word2index_dic, "rb")
            self.word_index = pickle.load(f)
            f.close()
        else:
            self.word_index = None

        #불용어 정의
        #관계언, 기호, 어미, 접미사를 제거한다.
        #https://docs.komoran.kr/firststep/postypes.html
        
        self.exclusion_tags = [
            'JKS', 'JKC', 'JKG', 'JKO', 'JKB', 'JKV', 'JKQ',
            'JX', 'JC',
            'SF', 'SP', 'SS', 'SE', 'SO',
            'EP', 'EF', 'EC', 'ETN', 'ETM',
            'XSN', 'XSV', 'XSA'
        ]
    
    #POS 태거를 호출
    #pos는 형태소를 추출한 뒤 pos를 태깅한다. 각 단어들 마다 pos와 함께 태깅을 하며 출력
    #http://docs.komoran.kr/firststep/postypes.html
    def pos(self, sentence):
        return self.komoran.pos(sentence)
    
    # 불용어 제거 후, 필요한 품사 정보만 가져오기
    # self.exclusion_tags에 포함되지 않는 품사인 경우, 키워드로 저장이 된다.
    def get_keywords(self, pos, without_tag=False):
        f = lambda x: x in self.exclusion_tags
        word_list = []
        for p in pos:
            if f(p[1]) is False:
                word_list.append(p if without_tag is False else p[0])
        return word_list

    # 키워드를 단어 인덱스 시퀀스로 변환
    def get_wordidx_sequence(self, keywords):
        if self.word_index is None:
            return []

        w2i = []
        for word in keywords:
            try:
                w2i.append(self.word_index[word])
            except KeyError:
                # 해당 단어가 사전에 없는 경우, OOV 처리
                w2i.append(self.word_index['OOV'])
        return w2i