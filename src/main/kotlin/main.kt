import java.io.File

/**
 * Searches this list or its range for an element having the key returned by the specified [selector] function
 * equal to the provided [key] value using the binary search algorithm.
 * The list is expected to be sorted into ascending order according to the Comparable natural ordering of keys of its elements.
 * otherwise the result is undefined.
 * * asdioasjdi
 *  * sajdioasjd
 * If the list contains multiple elements with the specified [key], there is no guarantee which one will be found.
 *
 * `null` value is considered to be less than any non-null value.
 *
 * @return the index of the element with the specified [key], if it is contained in the list within the specified range;
 * otherwise, the inverted insertion point `(-insertion point - 1)`.
 * The insertion point is defined as the index at which the element should be inserted,
 * so that the list (or the specified subrange of list) still remains sorted.
 * @sample samples.collections.Collections.Lists.binarySearchByKey
 */



fun main(args: Array<String>) {
    val b = "acegil"
    var it = b.iterator()
    it.next()
    println(String(it.asSequence().toList().toCharArray()))
//    for(i in 1..b.length){
//
//    }
//    if (args.isEmpty()) args[0] = "tagalogenglishen00niggrich_archive.torrent"
//    val torrentFile = File(args[0])
//    val reader = torrentFile.bufferedReader()
//    var input: List<String> = reader.readLines()
//    var parser = BencodeParser(input)
//    parser.decode()

//    println(BencodeParser(input))
}
