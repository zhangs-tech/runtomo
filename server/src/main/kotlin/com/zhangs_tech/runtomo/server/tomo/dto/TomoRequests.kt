package com.zhangs_tech.runtomo.server.tomo.dto

import java.util.UUID

data class RenameTomoRequest(val nickname: String)

data class EquipTomoRequest(val tomoId: UUID)
