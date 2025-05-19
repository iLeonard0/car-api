package com.unipar.carapi.car_api.controller

import com.unipar.carapi.car_api.entities.Car
import com.unipar.carapi.car_api.entities.Place
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/car")
class CarController {

    private val carMap = mutableMapOf<String, Car>()

    @PostMapping
    fun addCar(@RequestBody body: Any): ResponseEntity<Any> {
        when (body) {
            is Map<*, *> -> {
                val car = parseCar(body)
                return if (car != null) {
                    if (carMap.containsKey(car.id)) {
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID já existe")
                    } else {
                        carMap[car.id] = car
                        ResponseEntity.status(HttpStatus.CREATED).body(car)
                    }
                } else {
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON inválido")
                }
            }
            is List<*> -> {
                val added = mutableListOf<Car>()
                val errors = mutableListOf<String>()
                for (item in body) {
                    if (item is Map<*, *>) {
                        val car = parseCar(item)
                        if (car == null) {
                            errors.add("JSON inválido")
                        } else if (carMap.containsKey(car.id)) {
                            errors.add("ID ${car.id} já existe")
                        } else {
                            carMap[car.id] = car
                            added.add(car)
                        }
                    } else {
                        errors.add("JSON inválido na lista")
                    }
                }
                return if (errors.isEmpty()) {
                    ResponseEntity.status(HttpStatus.CREATED).body(added)
                } else {
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
                }
            }
            else -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON inválido")
            }
        }
    }

    @GetMapping
    fun getAllCars(): List<Car> = carMap.values.toList()

    @GetMapping("/{id}")
    fun getCar(@PathVariable id: String): ResponseEntity<Any> =
        carMap[id]?.let { ResponseEntity.ok(it) } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carro não encontrado")

    @PatchMapping("/{id}")
    fun updateCar(@PathVariable id: String, @RequestBody car: Car): ResponseEntity<Any> {
        return if (!carMap.containsKey(id)) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carro não encontrado")
        } else {
            carMap[id] = car
            ResponseEntity.ok(car)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteCar(@PathVariable id: String): ResponseEntity<Any> {
        return if (carMap.remove(id) != null) {
            ResponseEntity.ok("Carro deletado com sucesso")
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carro não encontrado")
        }
    }

    private fun parseCar(map: Map<*, *>): Car? {
        try {
            val id = map["id"] as? String ?: return null
            val imageUrl = map["imageUrl"] as? String ?: return null
            val year = map["year"] as? String ?: return null
            val name = map["name"] as? String ?: return null
            val licence = map["licence"] as? String ?: return null

            val placeMap = map["place"] as? Map<*, *> ?: return null
            val lat = placeMap["lat"] as? Double ?: return null
            val long = placeMap["long"] as? Double ?: return null

            return Car(id, imageUrl, year, name, licence, Place(lat, long))
        } catch (e: Exception) {
            return null
        }
    }
}