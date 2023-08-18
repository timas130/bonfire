package com.sup.dev.java.libs.http_api

import java.io.IOException

class ExceptionUnsafeRedirects : IOException("Unsafe redirect. To follow unsafe redirects, set followUnsafeRedirects to 'true'")
