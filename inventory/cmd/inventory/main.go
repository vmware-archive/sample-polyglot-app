package main

import (
	"encoding/json"
	"fmt"
	"os"
	"wavefront.com/polyglot/inventory/services/inventory"

	. "wavefront.com/polyglot/inventory/internal"
)

func main() {
	if len(os.Args) < 2 {
		fmt.Printf("Usage: inventory <config_file>\n")
		os.Exit(1)
	}

	InitGlobalConfig()

	file, ferr := os.Open(os.Args[1])
	if ferr != nil {
		fmt.Println(ferr)
		os.Exit(2)
	}
	if derr := json.NewDecoder(file).Decode(&GlobalConfig); derr != nil {
		fmt.Println(derr)
		os.Exit(2)
	}

	var server Server

	closer := NewGlobalTracer(GlobalConfig.Service)
	defer closer.Close()

	server = inventory.NewServer()
	if serr := server.Start(); serr != nil {
		fmt.Println(serr.Error())
		os.Exit(1)
	}
}
