package main

import (
	"fmt"
	"log"

	"net/http"

	"github.com/gin-gonic/gin"

	"example.com/marker/server/db"
	"example.com/marker/server/types"
)

func init() {
	log.SetPrefix("main: ")
	log.SetFlags(0)
}

func main() {
	fmt.Println("Starting server...")

	// init APIs
	router := gin.Default()

	router.GET("/", landingPage)

	router.POST("/mark", addMark)

	router.GET("/marks/:id", getMark)
	router.GET("/marks", getAllMarks)

	router.PUT("/mark", modifyMark)

	router.DELETE("/marks/:id", deleteMark)

	// run server
	router.Run("localhost:3333")
}

func landingPage(c *gin.Context) {
	//return built/compiled web content
	// c.File("../client")
}

func addMark(c *gin.Context) {
	var newMark types.Mark

	if err := c.BindJSON(&newMark); err != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": "Invalid JSON input"})
		return
	}

	fmt.Printf("New Mark: %v \n", newMark)

	if dbErr := db.AddMark(&newMark); dbErr != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": dbErr.Error()})
	} else {
		c.IndentedJSON(http.StatusCreated, newMark)
	}
}

func addMarks(c *gin.Context) {

}

func getMark(c *gin.Context) {
	id := c.Param("id")

	var mark types.Mark
	mark.ID = id

	if dbErr := db.GetMark(&mark); dbErr != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": dbErr.Error()})
	} else {
		c.IndentedJSON(http.StatusFound, mark)
	}
}

func getAllMarks(c *gin.Context) {
	marks, dbErr := db.GetAllMarks()

	if dbErr != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": dbErr.Error()})
	} else {
		c.IndentedJSON(http.StatusFound, marks)
	}

}

func modifyMark(c *gin.Context) {
	var modMark types.Mark

	if err := c.BindJSON(&modMark); err != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": "Invalid JSON input"})
		return
	}

	fmt.Printf("Modified Mark: %v \n", modMark)

	if dbErr := db.ModifyMark(&modMark); dbErr != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": dbErr.Error()})
	} else {
		c.IndentedJSON(http.StatusAccepted, modMark)
	}
}

func modifyMarks(c *gin.Context) {

}

func deleteMark(c *gin.Context) {
	id := c.Param("id")

	var mark types.Mark
	mark.ID = id

	if dbErr := db.DeleteMark(&mark); dbErr != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": dbErr.Error()})
	} else {
		c.IndentedJSON(http.StatusAccepted, mark)
	}
}

func deleteMarks(c *gin.Context) {

}

func deleteAllMarks(c *gin.Context) {

}
