#!/bin/bash

tmp=/tmp/check_genservices_temp
echo "" > $tmp

pushd .. > /dev/null
ant genservice
err=$?
popd > /dev/null

[[ $err -eq 0 ]] || exit $err

count()
{
	class=$1
	file=$2
	perl -e '
		$count = 0;
		while (<>)
		{
			++$count if /\b'$class'\b/;
		}
		print $count
	' $file
}

find .. \( -name \*.java -o -name \*.as \) -newer $tmp | while read file; do
	egrep ^import $file | sed -e 's/;//' | while read import; do
		class=${import##*.}
		# echo "Checking $class in $file"
		if [ $(count $class $file) -lt 2 ]; then 
			echo "ERROR: ${import} not used in file $file"
		fi
	done
done

rm $tmp
