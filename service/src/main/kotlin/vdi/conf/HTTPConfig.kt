package vdi.conf

import vdi.consts.ConfigDefault

data class HTTPConfig(val port: UShort = ConfigDefault.ServerPort)
