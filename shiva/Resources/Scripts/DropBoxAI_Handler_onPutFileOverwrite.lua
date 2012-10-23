--------------------------------------------------------------------------------
--  Handler.......... : onPutFileOverwrite
--  Author........... : Zachary Burke
--  Description...... : Writes the specified string to the specified file. Note
--                      that the filename given will be referenced from your
--                      Dropbox apps folder.
--
--                      Additionally, I wrote this to handle writes in the range
--                      of a couple bytes.  It may well fail if you try to write
--                      megabytes worth of data, I haven't tested it.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function DropBoxAI.onPutFileOverwrite ( file, content )
--------------------------------------------------------------------------------
	
    log.message("Writing to file: " .. file .. " with content: " .. content)
	system.callClientFunction ( "onDropBoxPutFileOverwrite", file, content )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
