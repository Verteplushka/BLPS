package com.example.BLPS.Entities;

public enum Privilege {
	VIEW_APPS,              // Просмотр приложений
	INSTALL_APP,            // Установка приложений
	RATE_APP,               // Оценка и написание отзывов

	UPLOAD_APP,             // Загрузка своего приложения
	DELETE_OWN_APP,         // Удаление своего приложения
	VIEW_STATS,             // Просмотр статистики по своему приложению
	RESPOND_TO_REVIEWS,     // Ответы на отзывы пользователей

	DELETE_ANY_APP,         // Удаление любых приложений
	BAN_USER,               // Блокировка пользователя
	APPROVE_APP,            // Модерация (одобрение приложений)
	VIEW_ALL_STATS          // Просмотр всех статистик
}
