#!/bin/bash

# This script works for one dataset at a time
# To run for all datasets inside a folder do the following:
# for dataset in $(find dataset/* -maxdepth 0 -type d); do ./treemaps.sh $dataset 1000 1000 output; done

print_invalid_usage(){
  echo "usage: ./treemaps.sh -input_dir -width -height -output_dir"
  echo "example: ./treemaps.sh dataset/exo 1000 1000 output"
}

check_args(){
  # Check number of arguments
  if [ $# -ne 4 ] ; then
    echo "Wrong number of arguments"
    print_invalid_usage
    exit
  fi

  # Check if input_dir exists
  if [ ! -d $1 ] ; then
    echo $1
    echo "Invalid input_dir"
    print_invalid_usage
    exit
  fi

  # Check if width and height are positive numbers
  re='^[0-9]+$'
  if ! [[ $2 =~ $re ]] ; then
     echo "Width is not a number"
     print_invalid_usage
     exit
  fi

  if ! [[ $3 =~ $re ]] ; then
     echo "Height is not a number"
     print_invalid_usage
     exit
  fi
}

check_args $*
input_dir=$1
width=$2
height=$3
output_dir=$4
dataset_name=$(basename $input_dir) # Extract dataset name -- end of path
input_dir=$(readlink -f $input_dir) # Get absolute path in case it isn't already
output_dir=$(readlink -f $output_dir)

date >> log
echo $* >> log

# Uncomment to run all techniques
# techniques='snd sqr otpbm otpbs ssv nmac nmew spiral strip'
techniques='nmac nmew'
for technique in $techniques
do
  complete_output_dir="$output_dir""/""$technique""/""$dataset_name" # Concatenate technique to output path
  echo $complete_output_dir
  java -cp ./bin com.ufrgs.Main $technique $input_dir $width $height $complete_output_dir >> log
done

# Run new technique - Greedy Insertion Treemap
#complete_output_dir="$output_dir""/git/""$dataset_name"
#echo $complete_output_dir
#java -cp ./bin com.eduardovernier.Main $input_dir $width $height $complete_output_dir >> log
#echo " " >> log

# Run Max's implementations
# First we need to put our datasets in his format
# usage: python v2s.py input_dataset_dir temp_folder
#temp_dir=$(readlink -f ./dataset-temp)
#mkdir $temp_dir
#python3 ./code/util/v2s.py $input_dir $temp_dir
#cd code/StableTreemap
# Ru his techniques
#techniques='moore hilb appr otpbss'
#for technique in $techniques
#do
  #complete_output_dir="$output_dir""/""$technique""/""$dataset_name" # Concatenate technique to output path
  #echo $complete_output_dir
  #mkdir $complete_output_dir
  #java -classpath "libraries/opencsv-3.7.jar:libraries/Jama-1.0.3.jar:src" UserControl/Simulator -technique $technique -inputfolder $temp_dir -outputfolder $complete_output_dir -width $width -height $height
#done

# Clean up temp datasets
#rm -rf $temp_dir

#cd code/StableTreemap
# Ru his techniques
#techniques='moore hilb appr ssv snd sqr otpbm otpbsize otpbsplit spiral strip'
#for technique in $techniques
#do
 #complete_output_dir="$output_dir""/""$technique""/""$dataset_name" # Concatenate technique to output path
# echo $complete_output_dir
# mkdir $complete_output_dir
# java -classpath "libraries/opencsv-3.7.jar:libraries/Jama-1.0.3.jar:src" UserControl/Simulator -technique $technique -inputfolder -baseline true $temp_dir -outputfolder $complete_output_dir -width $width -height $height
#done

Read-Host -Promt "press ENTER to exit"

