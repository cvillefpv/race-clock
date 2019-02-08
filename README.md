# Race Clock

Keep your race and practice sessions going with this simple clock. Written in ClojureScript and Re-frame. 

## Running The App

`lein figwheel dev`

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Usage

Just click start. The clock will run in a continuous loop that consists of a race period and break period to allow the next group of racers to get ready.

1. 3 second count down.
1. 2 minute race with a 30 second warning and 5 second count down.
1. 3 minute break with a one minute warning and 30 second warning. 
1. Loop back to the top.

This will continue till you click stop. 

You can modify the duration of the race and break periods by adjusting the `:clock-race-duration` and `:clock-break-duration`. This is found in the src/cljs/race_clock/db.cljs file. 

## Production Build

To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
