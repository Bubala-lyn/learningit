from django.urls import path

from . import views

urlpatterns = [
    # ex: /math_questions/content_list/
    path('content_list', views.questions, name='questions'),
    path('label_list', views.labels, name="labels"),
    path('cleaned_result', views.cleaned_data, name='cleaned data'),
    path('clean_data', views.clean, name='clean data'),
    path('data_summary', views.data_summary, name='data summary'),
    path('read_vector', views.read_vector, name='get formula vector'),
    path('parse_formula', views.read_encoded_tuple, name='parse formula'),
    path('preprocess', views.preprocess, name='data preprocess'),
    path('manual_tag', views.tag, name='manual tag'),
    path('manual_check', views.check_same_label, name='manual check'),
    path('clean_item', views.clean_item, name="clean user input"),
    path('search_question', views.search_question, name="search question"),
    path('word_count', views.word_count, name="word count"),
    path('make_label_emb', views.make_label_emb, name="label embedding"),
    path('search_label_emb', views.search_label_emb, name="search label vector")
]
