dns_cache_revoker
=================

This project provides methods to remove the dns cache entries in java runtime.

#problem:

Java caches a DNS entry after the first lookup but this is a problem if there is a ip address is
updated in the DNS server. There are quite a few ways to get around this but most of them invovle
updating the entry after certain amount of time.

For more information, please read the following link
https://docs.oracle.com/javase/7/docs/technotes/guides/net/properties.html

#Solution:

If you need a solution which would revoke the cache if there is a connection error then
`dns_cache_revoker` is a good option for you. All you need to do is revoke the cache by calling the
`revoker.revoke(hostname)` if there is a connection error.

