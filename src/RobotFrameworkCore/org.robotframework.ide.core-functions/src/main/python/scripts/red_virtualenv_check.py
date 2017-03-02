def is_virtualenv():
	import sys
	return hasattr(sys, 'real_prefix')

if __name__ == '__main__':
	print(is_virtualenv())