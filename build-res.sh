#!/bin/bash

files=`find svg/input/ -name *.svg -exec basename {} .svg \; | xargs echo`
for file in $files
do
  inkscape -z -e res/drawable-xhdpi/$file.png -w 128 -h 128 svg/input/$file.svg
  inkscape -z -e res/drawable-xhdpi/${file}_small.png -w 64 -h 64 svg/input/$file.svg

  inkscape -z -e res/drawable-hdpi/$file.png -w 96 -h 96 svg/input/$file.svg
  inkscape -z -e res/drawable-hdpi/${file}_small.png -w 48 -h 48 svg/input/$file.svg

  inkscape -z -e res/drawable-mdpi/$file.png -w 64 -h 64 svg/input/$file.svg
  inkscape -z -e res/drawable-mdpi/${file}_small.png -w 32 -h 32 svg/input/$file.svg

done
