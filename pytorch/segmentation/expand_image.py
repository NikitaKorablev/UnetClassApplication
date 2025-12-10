#!/usr/bin/env python3
"""
Скрипт для объединения 4 копий одного изображения в одно большее (2x2 сетка).
"""

import numpy as np
from PIL import Image
import os
import argparse


def create_large_image_from_copies(input_path, output_path=None):
    """
    Создает одно большое изображение из 4 копий входного изображения,
    размещая их в виде сетки 2x2.
    
    Args:
        input_path (str): Путь к входному изображению
        output_path (str): Путь для сохранения результата (по умолчанию input_path с добавлением "_expanded")
    
    Returns:
        str: Путь к сохраненному изображению
    """
    # Загрузка изображения
    img = Image.open(input_path)
    
    # Если не указан путь для вывода, создаем его на основе входного
    if output_path is None:
        name, ext = os.path.splitext(input_path)
        output_path = f"{name}_expanded{ext}"
    
    # Получаем размеры изображения
    width, height = img.size
    
    # Создаем новое изображение, которое будет в 2 раза больше по каждой оси
    large_img = Image.new(img.mode, (width * 3, height * 3))
    
    # Размещаем 4 копии изображения в сетке 2x2
    # Позиции: (0,0), (width,0), (0,height), (width,height)
    large_img.paste(img, (0, 0))                    # верхний левый
    large_img.paste(img, (width, 0))                 # верхний правый
    large_img.paste(img, (0, height))                # нижний левый
    large_img.paste(img, (width, height))            # нижний правый
    
    large_img.paste(img, (2*width, 0))
    large_img.paste(img, (2*width, height))
    large_img.paste(img, (2*width, 2*height))
    large_img.paste(img, (0, 2*height))
    large_img.paste(img, (width, 2*height))
    large_img.paste(img, (2*width, 2*height))


    # Сохраняем результат
    large_img.save(output_path)
    
    print(f"Создано большое изображение: {output_path}")
    print(f"Размер исходного: {width}x{height}")
    print(f"Размер результата: {width*3}x{height*3}")
    
    return output_path


def main():
    parser = argparse.ArgumentParser(description='Объединение 4 копий одного изображения в одно большее (2x2 сетка)')
    parser.add_argument('input', help='Путь к входному изображению')
    parser.add_argument('-o', '--output', help='Путь к выходному изображению (по умолчанию: input с _expanded)')
    
    args = parser.parse_args()
    
    # Проверяем, что входной файл существует
    if not os.path.exists(args.input):
        print(f"Ошибка: Входной файл '{args.input}' не найден.")
        return False
    
    # Создаем большое изображение
    output_path = create_large_image_from_copies(args.input, args.output)
    
    return True


if __name__ == "__main__":
    main()