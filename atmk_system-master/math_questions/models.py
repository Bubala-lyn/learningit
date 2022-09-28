import time
from django.db import models

# Create your models here.


class Content(models.Model):
    """题目模型类"""

    text = models.TextField(verbose_name="题目文本")
    label_img = models.CharField(max_length=625, verbose_name="题目标签图片")
    updated_at = models.IntegerField(default=time.time)
    created_at = models.IntegerField(default=time.time)

    class Meta:
        verbose_name = "数学题目"
        verbose_name_plural = "数学题目"


class Knowledge(models.Model):
    """知识点模型类"""

    uuid = models.CharField(max_length=100)
    parent_uuid = models.CharField(max_length=100)
    name = models.CharField(max_length=200)
    updated_at = models.IntegerField(default=time.time)
    created_at = models.IntegerField(default=time.time)

    class Meta:
        verbose_name = "知识点"
        verbose_name_plural = "知识点"


class KnowledgeTag(models.Model):
    """知识点标记模型类"""

    qid = models.BigIntegerField()
    label_id = models.BigIntegerField()
    updated_at = models.IntegerField(default=time.time)
    created_at = models.IntegerField(default=time.time)

    class Meta:
        verbose_name = "知识点标记"
        verbose_name_plural = "知识点标记"
