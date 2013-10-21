# ic
### so far a basic first example

## set algorithm
* lein run -a md5
* lein run -a sha-1
* lein run -a sha-256
* lein run -a sha-512

## set interval for next scan in seconds
* lein run -c 30   ;;(rescan after 30 seconds)
* lein run -c 2592000  ;;(rescan after 1 month)

## add new store
* lein run -s storename -p /path/to/store -n
* lein run -s otherstore -p /path/to/other/store -n

## delete a store
* lein run -s storename -d

## list stores
* lein run -l

## rescan store by name
* lein run -s storename -r

## rescan all stores
* lein run -R

## show basic stats
* lein run -i

## show data in gui
* lein run -g
