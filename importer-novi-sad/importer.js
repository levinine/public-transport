const axios = require('axios')
const fs = require('fs')
const osmosis = require('osmosis')
const _ = require('underscore')

const linesData = require('./data/lines.json')
const stopsData = require('./data/stops.json')
const timeTableDataWorkday = require('./data/time-tables-R.json')
const timeTableDataSaturday = require('./data/time-tables-S.json')
const timeTableDataSunday = require('./data/time-tables-N.json')

async function fetchLines () {
    const url = 'http://gspns.rs/mreza'
    let p = new Promise((resolve, reject) => {
        let lines = []
        osmosis.get(url)
            .find('#button-linije ul li')
            .set({
                id: 'a @id',
                title: 'a @title',
                name: 'a',
                type: 'a @class'
            })
            .data(line => {
                if (line.type.indexOf(' medjumesni ') !== -1) {
                    line.type = 3
                } else if (line.type.indexOf(' prigrad ') !== -1) {
                    line.type = 2
                } else {
                    line.type = 1
                }
                lines.push(line)
            })
            .done(() => {
                resolve(lines)
                // fs.writeFile('data-lines2.json', JSON.stringify(lines, null, 2), 'utf8', (err) => {
                //     if (err) throw err
                //     console.log('Lines\' coordinates saved!')
                // })
            })
            // .log(console.log)
            .error(reject)
    })
    return p;
}

function fetchLine (lineNumber) {
    return axios.get(`http://www.gspns.co.rs/mreza-get-linija-tacke?linija=${lineNumber}`)
        .then(response => {
            // console.log(`retrieved line ${lineNumber}`)
            return response.data
                .filter(coordStr => { return coordStr.split(',').length === 2 })
                .map(coordStr => { return { lat: coordStr.split(',')[0].trim(), lon: coordStr.split(',')[1].trim() } })
        })
}

async function fetchLinesCoordinates (linesData) {
    const linesPromises = linesData.map(line => {
        return fetchLine(line.id).then(coords => line.coordinates = coords)
    })
    await Promise.all(linesPromises)
    fs.writeFile('data/lines.json', JSON.stringify(linesData, null, 2), 'utf8', (err) => {
        if (err) throw err
        console.log('Lines\' coordinates saved!')
    })
    return linesData
}

async function fetchStops (linesData) {
    let allStops = []
    const stopsPromises = linesData.map(line => {
        return axios.get(`http://www.gspns.co.rs/mreza-get-stajalista-tacke?linija=${line.id}`)
            .then(response => {
                let stops = response.data
                    // [1A],[1ZA],[3B],[8A],[19A]|19.8416504854|45.255203707|USPENSKA - \u0160AFARIKOVA|http:\/\/www.mapanovisad.rs\/stajalista_gsp\/img002617.jpg|I
                    .map(stopStr => stopStr.split('|'))
                    .map(stopArray => { 
                        return {
                            lines: stopArray[0].trim().split(',').map(line =>  line.replace(/\[(.*)\]/, '$1')),
                            lat: stopArray[2].trim(),
                            lon: stopArray[1].trim(),
                            name: stopArray[3],
                            photo: stopArray[4],
                            zone: stopArray[5]
                        }
                    })
                allStops.push(stops)
            })
    })
    await Promise.all(stopsPromises)
    allStops = _.unique(allStops, false, JSON.stringify)
    fs.writeFile('data/stops.json', JSON.stringify(allStops, null, 2), 'utf8', (err) => {
        if (err) throw err
        console.log('Stops\' coordinates saved!')
    })
    return allStops
}

async function fetchAll () {
    let lines = await fetchLines()
    fetchLinesCoordinates(lines)
    fetchStops(lines)
    // line 17b is missing, add it manually
    // add17b(lines)
    fetchTimeTables()
}

function add17b (lines) {
    let l17 = _.clone(_.find(lines, line => line.name === '17A'))
    l17.name = '17B'
    l17.id = 32
    lines.push(l17)
}

async function fetchTimeTables () {
    let lines = []
    let timeTableMeta = await fetchTimeTableMeta()
    let linesPromises = timeTableMeta.lineTypes.map(async lineType => {
        return fetchTimeTableLines(lineType, timeTableMeta.datesFrom[0], timeTableMeta.dayTypes[0])
            .then(fetchedLines => {
                lines.push(...fetchedLines)
            })
    })
    await Promise.all(linesPromises)
    console.log(`fetched lines ${JSON.stringify(lines)}`)
    for (let dayType of timeTableMeta.dayTypes) {
        linesPromises = lines.map(line => {
            return fetchTimeTable(line.lineType, timeTableMeta.datesFrom[0], dayType, line.key).then(tt => {
                line.timeTable = tt
            }).catch(error => {
                console.log(`failed fetching time table for ${line}`)
                console.log(error)
            })
        })
        await Promise.all(linesPromises)

        // lines 7, 11, 18 have A and B variants - merge them
        let mergedLines = lines
        _.each([7, 11, 18], num => {
            let dupes = _.filter(mergedLines, line => line.key.indexOf(`${num}A`) === 0 || line.key.indexOf(`${num}B`) === 0)
            let a = dupes[0].key.indexOf('A') !== -1 ? dupes[0] : dupes[1]
            let b = dupes[0].key.indexOf('A') !== -1 ? dupes[1] : dupes[0]
            a.timeTable[1] = b.timeTable[0]
            a.name = a.name + ' / ' + b.name
            mergedLines = _.without(mergedLines, b)
        })

        fs.writeFile(`data/time-tables-${dayType}.json`, JSON.stringify(mergedLines, null, 2), 'utf8', (err) => {
            if (err) throw err
            console.log(`Time tables ${dayType} saved!`)
        })
    }
}

async function fetchTimeTableMeta () {
    const url = 'http://gspns.rs/red-voznje/gradski'
    let p = new Promise((resolve, reject) => {
        osmosis.get(url)
            .find('#rvform')
            .set({
                datesFrom: ['#vaziod option @value'],
                lineTypes: ['#rv option @value'],
                dayTypes: ['#dan option @value']
            })
            .data(data => {
                // console.log(`data ${JSON.stringify(data)}`)
                resolve(data)
            })
            // .log(console.log)
            .error(reject)
    })
    return p;
}

async function fetchTimeTableLines (lineType, dateFrom, dayType) {
    const url = `http://gspns.rs/red-voznje/lista-linija?rv=${lineType}&vaziod=${dateFrom}&dan=${dayType}`
    let lines = []
    let p = new Promise((resolve, reject) => {
        osmosis.get(url)
            .find('#linija option')
            .set({
                key: '. @value',
                name: '.'
            })
            .data(data => {
                data.lineType = lineType
                lines.push(data)
            })
            // .log(console.log)
            .error(reject)
            .done(() => {
                resolve(lines)
            })
    })
    return p;
}

const strRegex = /[^0-9]*/gi
const numRegex = /\d*/gi

function fetchTimeTable (lineType, dateFrom, dayType, line) {
    const url = `http://gspns.rs/red-voznje/ispis-polazaka?rv=${lineType}&vaziod=${dateFrom}&dan=${dayType}&linija%5B%5D=${line}`
    // console.log(`fetching time tables for line ${line} from url ${url}`)
    let p = new Promise((resolve, reject) => {
        let timetable = [[],[]]
        let currentHour = ''
        let dir = 0;
        osmosis.get(url)
            .find('tr > td > b, tr > td > sup > font > span')
            .set('d')
            .then((context, data) => {
                // minutes have a 'b' tag inside, hours don't
                if (context.find('b').length === 0) {
                    if ((currentHour > data.d && data.d !== '00') || currentHour === '00') {
                        dir = 1
                    }
                    currentHour = data.d
                } else {
                    timetable[dir].push({
                        formatted: `${currentHour}:${data.d}`,
                        hour: currentHour,
                        minute: data.d.replace(strRegex, ''),
                        label: data.d.replace(numRegex, '')
                    })
                }
                if (context.last) {
                    resolve(timetable)
                }
            })
            //.log(console.log)
            .error(reject)
    })
    return p;
}

function createAllTestData (name) {
    let lines = linesData.map(lineData => lineData.name)
    return createTestData(name, lines)
}
function createTestData (name, lines) {
    let testData = {}

    testData.lines = linesData
        .filter(lineData => lines.includes(lineData.name))
        .map(lineData => {
            return {
                name: lineData.name,
                description: lineData.title,
                coordinates: shortenCoordsList(lineData.coordinates),
                timeTable: getTimeTable(lineData.name),
                timeTableSaturday: getTimeTable(lineData.name, 'saturday'),
                timeTableSunday: getTimeTable(lineData.name, 'sunday')
            }
        })
    
    testData.stops = stopsData
        .filter(stop => _.intersection(lines, stop.lines).length)
        .map(stop => {
            return {
                name: stop.name,
                lat: stop.lat,
                lon: stop.lon,
                zone: stop.zone,
                lines: _.intersection(lines, stop.lines)
            }
        })

    fs.writeFile(`test-data/${name}.json`, JSON.stringify(testData, null, 2), 'utf8', (err) => {
        if (err) throw err
        console.log(`Test data ${name} saved!`)
    })
}

function getTimeTable (name, dayType) {
    try {
        let timeTableData = timeTableDataWorkday
        if (dayType === 'saturday') {
            timeTableData = timeTableDataSaturday
        } else if (dayType === 'sunday') {
            timeTableData = timeTableDataSunday
        }
        let lineKey = name.substring(0, name.length - 1) + ' '
        let direction = name.substring(name.length - 1)
        let line = timeTableData.find(tt => tt.name.indexOf(lineKey) === 0)
        if (!line) {
            line = timeTableData.find(tt => tt.name.indexOf(name + ' ') === 0)
        }
        if (line) {
            return line.timeTable[direction === 'A' ? 0 : 1].map(departure => `${departure.hour}:${departure.minute}`)
        } else {
            console.log(`couldn't get time-table for ${name}`)
            return []
        }
    } catch (error) {
        console.log(`getTimeTable failed for ${name} and ${dayType}`)
        return []
    }
}

function distance (lat1, lon1, lat2, lon2) {
    var p = 0.017453292519943295;    // Math.PI / 180
    var c = Math.cos;
    var a = 0.5 - c((lat2 - lat1) * p)/2 + 
            c(lat1 * p) * c(lat2 * p) * 
            (1 - c((lon2 - lon1) * p))/2;
  
    return 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
}

function shortenCoordsList (coordinates) {
    let shortened = [], skipCount = 0
    for (let i = 0; i < coordinates.length; i++) {
        if (i === 0 || i === coordinates.length - 1 || skipCount > 5) {
            shortened.push(coordinates[i])
            skipCount = 0
        } else {
            let a = shortened[shortened.length - 1], b = coordinates[i], c = coordinates[i + 1]
            let ab = distance(+a.lat, +a.lon, +b.lat, +b.lon)
            let ac = distance(+a.lat, +a.lon, +c.lat, +c.lon)
            let bc = distance(+b.lat, +b.lon, +c.lat, +c.lon)
            if ((ab + bc) / ac > 1.01) {
                shortened.push(coordinates[i])
                skipCount = 0
            } else {
                skipCount++
            }
        }
    }
    return shortened
}

const cityLines = ["1A","1B","2A","2B","3A","3B","4A","4B","5A","5B","6A","6B","7A","7B","8A","8B","9A","9B","10A","10B","11A","11B","12A","12B","13A","13B","14A","14B","15A","15B","16A","16B","17A","21A","21B","68A","68B","69A","69B","71A","71B","18A","18B","52A","52B","53A","53B","54A","54B","55A","55B","56A","56B"]

let command = process.argv[2]
console.log(command)
if (command === 'produce') {
    console.log('creating final output')
    createAllTestData('lines')
} else {
    console.log('fetching data')
    fetchAll()
}
