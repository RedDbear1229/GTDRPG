package com.questlog.core.domain.model

enum class TaskStatus { INBOX, ACTIVE, WAITING, SOMEDAY, REFERENCE, DONE, DELETED }

enum class ProjectStatus { ACTIVE, ON_HOLD, COMPLETED, SOMEDAY }

enum class CaptureSource { APP, WIDGET, SHARE, VOICE, NOTIFICATION }

enum class ClarifyResultType { TASK, SOMEDAY, REFERENCE, DELETED, DONE_NOW }
