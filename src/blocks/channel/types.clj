(ns blocks.channel.types)

(def Channel ::channeltype-Channel)


(def Bitmap ::channeltype-Bitmap)
(def JPEG   ::channeltype-JPEG)
(def PNG    ::channeltype-PNG)
(def Kernel ::channeltype-Kernel)

; Derived classes
; (def NewChannel ::channeltype-NewChannel)

(def types-list [Bitmap JPEG PNG Kernel])