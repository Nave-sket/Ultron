package com.example.model

/**
 * Procedural Generator for an Original Futuristic Cybernetic AI Skull 3D Mesh
 *
 * Designed with:
 * - Neuro-cranium vault (forehead, parietal plates, temporal cyber-chambers)
 * - Brow ridge with glowing optic ocular sockets
 * - Zygomatic cybernetic arches (cheekbones)
 * - Articulated Mandible & Maxilla (jaw structure that opens/closes dynamically for real-time lip-sync)
 * - Occipital base and cervical spine telemetry pillar
 * - Internal floating neural reactor core
 * - Dual-hemisphere cybernetic surface circuit traces
 */
object CyberneticSkullFactory {

    fun createCyberneticSkull(): CyberneticSkullMesh {
        val vertices = mutableListOf<SkullVertex>()
        val wires = mutableListOf<CyberWire>()
        val facets = mutableListOf<CyberFacet>()

        fun addVertex(v: SkullVertex): Int {
            vertices.add(v)
            return vertices.size - 1
        }

        fun addWire(
            idx1: Int,
            idx2: Int,
            isJaw: Boolean = false,
            isCircuit: Boolean = false,
            isFacial: Boolean = true,
            alpha: Float = 0.85f
        ) {
            wires.add(
                CyberWire(
                    v1Index = idx1,
                    v2Index = idx2,
                    isJawWire = isJaw,
                    isCircuitPath = isCircuit,
                    isFacialContour = isFacial,
                    baseAlpha = alpha
                )
            )
        }

        fun addFacet(idx1: Int, idx2: Int, idx3: Int, isJaw: Boolean = false, alpha: Float = 0.10f) {
            facets.add(CyberFacet(idx1, idx2, idx3, isJaw, alpha))
        }

        // =========================================================================
        // 1. NEURAL CORE & INTERNAL ARCHITECTURE (Layer 0)
        // =========================================================================
        val coreCenterIdx = addVertex(SkullVertex(Vector3D(0f, -10f, 0f), isJaw = false, layer = 0, circuitIntensity = 1.0f))

        // Inner Core Ring nodes
        val coreNodes = mutableListOf<Int>()
        val coreCount = 8
        for (i in 0 until coreCount) {
            val ang = i * (2f * Math.PI.toFloat() / coreCount)
            val rad = 32f
            val cx = (Math.cos(ang.toDouble()) * rad).toFloat()
            val cy = -10f + (Math.sin(ang.toDouble()) * 12f).toFloat()
            val cz = (Math.sin(ang.toDouble()) * rad).toFloat()
            val idx = addVertex(SkullVertex(Vector3D(cx, cy, cz), isJaw = false, layer = 0, circuitIntensity = 0.9f))
            coreNodes.add(idx)
            addWire(coreCenterIdx, idx, isJaw = false, isCircuit = true, alpha = 0.6f)
        }
        for (i in 0 until coreCount) {
            val next = (i + 1) % coreCount
            addWire(coreNodes[i], coreNodes[next], isJaw = false, isCircuit = true, alpha = 0.5f)
        }

        // =========================================================================
        // 2. CRANIAL VAULT (Forehead, Top of Skull, Temporal Plates - Layer 1 & 2)
        // =========================================================================
        val crownTopIdx = addVertex(SkullVertex(Vector3D(0f, -135f, 0f), layer = 2, circuitIntensity = 0.8f))
        val crownFrontIdx = addVertex(SkullVertex(Vector3D(0f, -125f, 55f), layer = 2, circuitIntensity = 0.85f))
        val crownBackIdx = addVertex(SkullVertex(Vector3D(0f, -120f, -70f), layer = 1, circuitIntensity = 0.6f))

        // Forehead Plates
        val foreheadCenter = addVertex(SkullVertex(Vector3D(0f, -95f, 92f), layer = 1, circuitIntensity = 0.9f))
        val foreheadLeft = addVertex(SkullVertex(Vector3D(-42f, -90f, 85f), layer = 1, circuitIntensity = 0.75f))
        val foreheadRight = addVertex(SkullVertex(Vector3D(42f, -90f, 85f), layer = 1, circuitIntensity = 0.75f))

        val foreheadFarLeft = addVertex(SkullVertex(Vector3D(-74f, -80f, 50f), layer = 1, circuitIntensity = 0.65f))
        val foreheadFarRight = addVertex(SkullVertex(Vector3D(74f, -80f, 50f), layer = 1, circuitIntensity = 0.65f))

        // Cranial Wires & Facets
        addWire(crownTopIdx, crownFrontIdx, isCircuit = true)
        addWire(crownFrontIdx, foreheadCenter, isCircuit = true)
        addWire(crownTopIdx, crownBackIdx)

        addWire(foreheadCenter, foreheadLeft)
        addWire(foreheadCenter, foreheadRight)
        addWire(foreheadLeft, foreheadFarLeft)
        addWire(foreheadRight, foreheadFarRight)

        addWire(crownFrontIdx, foreheadLeft)
        addWire(crownFrontIdx, foreheadRight)

        addFacet(crownFrontIdx, foreheadCenter, foreheadLeft, alpha = 0.12f)
        addFacet(crownFrontIdx, foreheadCenter, foreheadRight, alpha = 0.12f)

        // Parietal / Temporal Sides
        val temporalLeft = addVertex(SkullVertex(Vector3D(-86f, -45f, 15f), layer = 1, circuitIntensity = 0.7f))
        val temporalRight = addVertex(SkullVertex(Vector3D(86f, -45f, 15f), layer = 1, circuitIntensity = 0.7f))
        val occipitalLeft = addVertex(SkullVertex(Vector3D(-65f, -30f, -70f), layer = 1, circuitIntensity = 0.5f))
        val occipitalRight = addVertex(SkullVertex(Vector3D(65f, -30f, -70f), layer = 1, circuitIntensity = 0.5f))
        val occipitalCenter = addVertex(SkullVertex(Vector3D(0f, -25f, -85f), layer = 1, circuitIntensity = 0.5f))

        addWire(foreheadFarLeft, temporalLeft)
        addWire(foreheadFarRight, temporalRight)
        addWire(temporalLeft, occipitalLeft)
        addWire(temporalRight, occipitalRight)
        addWire(occipitalLeft, occipitalCenter)
        addWire(occipitalRight, occipitalCenter)
        addWire(crownBackIdx, occipitalCenter)

        // =========================================================================
        // 3. BROW RIDGE & OCULAR EYE SOCKETS (Layer 1 & Eyes)
        // =========================================================================
        val glabella = addVertex(SkullVertex(Vector3D(0f, -60f, 102f), layer = 1, circuitIntensity = 0.95f))
        val browLeftInner = addVertex(SkullVertex(Vector3D(-24f, -62f, 100f), layer = 1, circuitIntensity = 0.9f))
        val browRightInner = addVertex(SkullVertex(Vector3D(24f, -62f, 100f), layer = 1, circuitIntensity = 0.9f))
        val browLeftOuter = addVertex(SkullVertex(Vector3D(-62f, -55f, 85f), layer = 1, circuitIntensity = 0.85f))
        val browRightOuter = addVertex(SkullVertex(Vector3D(62f, -55f, 85f), layer = 1, circuitIntensity = 0.85f))

        addWire(foreheadCenter, glabella, isCircuit = true)
        addWire(glabella, browLeftInner)
        addWire(glabella, browRightInner)
        addWire(browLeftInner, browLeftOuter)
        addWire(browRightInner, browRightOuter)
        addWire(foreheadLeft, browLeftInner)
        addWire(foreheadRight, browRightInner)
        addWire(foreheadFarLeft, browLeftOuter)
        addWire(foreheadFarRight, browRightOuter)

        // Eye Socket Cavities (Inner Orbital Rings)
        val eyeSocketLeftLower = addVertex(SkullVertex(Vector3D(-42f, -20f, 90f), layer = 1, circuitIntensity = 0.8f))
        val eyeSocketRightLower = addVertex(SkullVertex(Vector3D(42f, -20f, 90f), layer = 1, circuitIntensity = 0.8f))
        val eyeSocketLeftInner = addVertex(SkullVertex(Vector3D(-16f, -35f, 96f), layer = 1, circuitIntensity = 0.8f))
        val eyeSocketRightInner = addVertex(SkullVertex(Vector3D(16f, -35f, 96f), layer = 1, circuitIntensity = 0.8f))

        addWire(browLeftInner, eyeSocketLeftInner)
        addWire(browRightInner, eyeSocketRightInner)
        addWire(eyeSocketLeftInner, eyeSocketLeftLower)
        addWire(eyeSocketRightInner, eyeSocketRightLower)
        addWire(browLeftOuter, eyeSocketLeftLower)
        addWire(browRightOuter, eyeSocketRightLower)

        // Glowing Optic Centers (Left and Right Eyes)
        val leftEyeIdx = addVertex(SkullVertex(Vector3D(-38f, -42f, 82f), isEye = true, layer = 2, circuitIntensity = 1.0f))
        val rightEyeIdx = addVertex(SkullVertex(Vector3D(38f, -42f, 82f), isEye = true, layer = 2, circuitIntensity = 1.0f))

        addWire(leftEyeIdx, eyeSocketLeftInner, isCircuit = true, alpha = 0.8f)
        addWire(leftEyeIdx, browLeftInner, isCircuit = true, alpha = 0.8f)
        addWire(leftEyeIdx, eyeSocketLeftLower, isCircuit = true, alpha = 0.8f)
        addWire(rightEyeIdx, eyeSocketRightInner, isCircuit = true, alpha = 0.8f)
        addWire(rightEyeIdx, browRightInner, isCircuit = true, alpha = 0.8f)
        addWire(rightEyeIdx, eyeSocketRightLower, isCircuit = true, alpha = 0.8f)

        // =========================================================================
        // 4. NASAL CAVITY & UPPER MAXILLA (Layer 1)
        // =========================================================================
        val nasalBridge = addVertex(SkullVertex(Vector3D(0f, -40f, 105f), layer = 1, circuitIntensity = 0.85f))
        val nasalAperture = addVertex(SkullVertex(Vector3D(0f, -12f, 95f), layer = 1, circuitIntensity = 0.8f))
        val maxillaLeft = addVertex(SkullVertex(Vector3D(-26f, -2f, 90f), layer = 1, circuitIntensity = 0.75f))
        val maxillaRight = addVertex(SkullVertex(Vector3D(26f, -2f, 90f), layer = 1, circuitIntensity = 0.75f))
        val upperTeethCenter = addVertex(SkullVertex(Vector3D(0f, 15f, 92f), layer = 1, circuitIntensity = 0.9f))
        val upperTeethLeft = addVertex(SkullVertex(Vector3D(-24f, 16f, 84f), layer = 1, circuitIntensity = 0.8f))
        val upperTeethRight = addVertex(SkullVertex(Vector3D(24f, 16f, 84f), layer = 1, circuitIntensity = 0.8f))

        addWire(glabella, nasalBridge, isCircuit = true)
        addWire(nasalBridge, nasalAperture, isCircuit = true)
        addWire(nasalAperture, maxillaLeft)
        addWire(nasalAperture, maxillaRight)
        addWire(nasalAperture, upperTeethCenter, isCircuit = true)
        addWire(upperTeethCenter, upperTeethLeft)
        addWire(upperTeethCenter, upperTeethRight)
        addWire(maxillaLeft, upperTeethLeft)
        addWire(maxillaRight, upperTeethRight)

        // Connect eyes to maxilla
        addWire(eyeSocketLeftInner, nasalBridge)
        addWire(eyeSocketRightInner, nasalBridge)

        // Zygomatic Arches (Cheekbones)
        val cheekLeft = addVertex(SkullVertex(Vector3D(-76f, -15f, 68f), layer = 1, circuitIntensity = 0.8f))
        val cheekRight = addVertex(SkullVertex(Vector3D(76f, -15f, 68f), layer = 1, circuitIntensity = 0.8f))

        addWire(eyeSocketLeftLower, cheekLeft)
        addWire(eyeSocketRightLower, cheekRight)
        addWire(browLeftOuter, cheekLeft)
        addWire(browRightOuter, cheekRight)
        addWire(cheekLeft, temporalLeft)
        addWire(cheekRight, temporalRight)
        addWire(cheekLeft, maxillaLeft)
        addWire(cheekRight, maxillaRight)

        addFacet(nasalBridge, nasalAperture, maxillaLeft, alpha = 0.15f)
        addFacet(nasalBridge, nasalAperture, maxillaRight, alpha = 0.15f)
        addFacet(eyeSocketLeftLower, maxillaLeft, cheekLeft, alpha = 0.12f)
        addFacet(eyeSocketRightLower, maxillaRight, cheekRight, alpha = 0.12f)

        // =========================================================================
        // 5. ARTICULATED MANDIBLE (Lower Jaw for Real-time Lip-sync Speech)
        // These vertices have isJaw = true and will translate & rotate when speaking!
        // =========================================================================
        // Lower Teeth & Chin
        val lowerTeethCenter = addVertex(SkullVertex(Vector3D(0f, 25f, 90f), isJaw = true, layer = 1, circuitIntensity = 0.95f))
        val lowerTeethLeft = addVertex(SkullVertex(Vector3D(-22f, 26f, 82f), isJaw = true, layer = 1, circuitIntensity = 0.8f))
        val lowerTeethRight = addVertex(SkullVertex(Vector3D(22f, 26f, 82f), isJaw = true, layer = 1, circuitIntensity = 0.8f))

        val chinCenter = addVertex(SkullVertex(Vector3D(0f, 62f, 85f), isJaw = true, layer = 2, circuitIntensity = 0.95f))
        val chinLeft = addVertex(SkullVertex(Vector3D(-24f, 60f, 78f), isJaw = true, layer = 1, circuitIntensity = 0.85f))
        val chinRight = addVertex(SkullVertex(Vector3D(24f, 60f, 78f), isJaw = true, layer = 1, circuitIntensity = 0.85f))

        // Jaw Angles / Ramus (hinge area)
        val jawAngleLeft = addVertex(SkullVertex(Vector3D(-64f, 32f, 25f), isJaw = true, layer = 1, circuitIntensity = 0.75f))
        val jawAngleRight = addVertex(SkullVertex(Vector3D(64f, 32f, 25f), isJaw = true, layer = 1, circuitIntensity = 0.75f))
        val jawHingeLeft = addVertex(SkullVertex(Vector3D(-70f, 5f, 5f), isJaw = true, layer = 1, circuitIntensity = 0.7f))
        val jawHingeRight = addVertex(SkullVertex(Vector3D(70f, 5f, 5f), isJaw = true, layer = 1, circuitIntensity = 0.7f))

        // Lower Jaw Wireframe
        addWire(lowerTeethCenter, lowerTeethLeft, isJaw = true)
        addWire(lowerTeethCenter, lowerTeethRight, isJaw = true)
        addWire(lowerTeethCenter, chinCenter, isJaw = true, isCircuit = true)
        addWire(chinCenter, chinLeft, isJaw = true)
        addWire(chinCenter, chinRight, isJaw = true)
        addWire(lowerTeethLeft, chinLeft, isJaw = true)
        addWire(lowerTeethRight, chinRight, isJaw = true)

        addWire(chinLeft, jawAngleLeft, isJaw = true)
        addWire(chinRight, jawAngleRight, isJaw = true)
        addWire(jawAngleLeft, jawHingeLeft, isJaw = true)
        addWire(jawAngleRight, jawHingeRight, isJaw = true)
        addWire(lowerTeethLeft, jawAngleLeft, isJaw = true)
        addWire(lowerTeethRight, jawAngleRight, isJaw = true)

        // Hinge connection to skull base (temporomandibular joint)
        addWire(jawHingeLeft, cheekLeft, isJaw = false, alpha = 0.5f)
        addWire(jawHingeRight, cheekRight, isJaw = false, alpha = 0.5f)

        // Jaw Facets
        addFacet(lowerTeethCenter, chinCenter, chinLeft, isJaw = true, alpha = 0.16f)
        addFacet(lowerTeethCenter, chinCenter, chinRight, isJaw = true, alpha = 0.16f)
        addFacet(chinLeft, lowerTeethLeft, jawAngleLeft, isJaw = true, alpha = 0.12f)
        addFacet(chinRight, lowerTeethRight, jawAngleRight, isJaw = true, alpha = 0.12f)

        // =========================================================================
        // 6. CERVICAL CYBER-SPINE & SKULL BASE (Layer 1)
        // =========================================================================
        val spineNode1 = addVertex(SkullVertex(Vector3D(0f, 40f, -30f), layer = 1, circuitIntensity = 0.7f))
        val spineNode2 = addVertex(SkullVertex(Vector3D(0f, 75f, -25f), layer = 1, circuitIntensity = 0.65f))
        val spineNode3 = addVertex(SkullVertex(Vector3D(0f, 110f, -20f), layer = 1, circuitIntensity = 0.6f))

        val spinePlate1L = addVertex(SkullVertex(Vector3D(-28f, 50f, -32f), layer = 1, circuitIntensity = 0.6f))
        val spinePlate1R = addVertex(SkullVertex(Vector3D(28f, 50f, -32f), layer = 1, circuitIntensity = 0.6f))
        val spinePlate2L = addVertex(SkullVertex(Vector3D(-24f, 85f, -28f), layer = 1, circuitIntensity = 0.55f))
        val spinePlate2R = addVertex(SkullVertex(Vector3D(24f, 85f, -28f), layer = 1, circuitIntensity = 0.55f))

        addWire(occipitalCenter, spineNode1, isCircuit = true)
        addWire(spineNode1, spineNode2, isCircuit = true)
        addWire(spineNode2, spineNode3, isCircuit = true)

        addWire(spineNode1, spinePlate1L)
        addWire(spineNode1, spinePlate1R)
        addWire(spinePlate1L, spinePlate2L)
        addWire(spinePlate1R, spinePlate2R)
        addWire(spineNode2, spinePlate2L)
        addWire(spineNode2, spinePlate2R)

        // =========================================================================
        // 7. CYBERNETIC CIRCUIT TRACES (Energy pulses travel along these)
        // =========================================================================
        // Left hemisphere circuit line
        addWire(coreCenterIdx, glabella, isCircuit = true, alpha = 0.7f)
        addWire(coreCenterIdx, crownTopIdx, isCircuit = true, alpha = 0.7f)
        addWire(foreheadLeft, temporalLeft, isCircuit = true, alpha = 0.7f)
        addWire(foreheadRight, temporalRight, isCircuit = true, alpha = 0.7f)
        addWire(glabella, upperTeethCenter, isCircuit = true, alpha = 0.9f)

        return CyberneticSkullMesh(
            vertices = vertices,
            wires = wires,
            facets = facets,
            leftEyeIndex = leftEyeIdx,
            rightEyeIndex = rightEyeIdx,
            coreIndex = coreCenterIdx
        )
    }
}
