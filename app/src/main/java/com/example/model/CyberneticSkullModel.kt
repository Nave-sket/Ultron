package com.example.model

/**
 * 3D Vector in holographic coordinate space
 */
data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar)
}

/**
 * 3D Vertex in a cybernetic skull mesh
 * @param isJaw Whether this vertex belongs to the articulated lower jaw/mandible for lip-sync
 * @param layer Holographic depth layer (0 = inner core, 1 = skull surface, 2 = outer cybernetic plating/horns)
 * @param circuitIntensity Brightness of cybernetic circuit paths running through this node
 */
data class SkullVertex(
    val basePos: Vector3D,
    val isJaw: Boolean = false,
    val isEye: Boolean = false,
    val layer: Int = 1,
    val circuitIntensity: Float = 0.5f
)

/**
 * Line segment connecting two vertices in the cybernetic 3D wireframe
 */
data class CyberWire(
    val v1Index: Int,
    val v2Index: Int,
    val isJawWire: Boolean = false,
    val isCircuitPath: Boolean = false,
    val isFacialContour: Boolean = true,
    val baseAlpha: Float = 0.85f
)

/**
 * Polygonal facet for semi-transparent holographic depth rendering
 */
data class CyberFacet(
    val v1Index: Int,
    val v2Index: Int,
    val v3Index: Int,
    val isJawFacet: Boolean = false,
    val fillAlpha: Float = 0.12f
)

/**
 * Cybernetic AI Skull 3D Model specification
 */
data class CyberneticSkullMesh(
    val vertices: List<SkullVertex>,
    val wires: List<CyberWire>,
    val facets: List<CyberFacet>,
    val leftEyeIndex: Int,
    val rightEyeIndex: Int,
    val coreIndex: Int
)
