<p align="center">
  <img src="http://static.craftmend.com/fireio/FIREIO.png" />
</p>

# Fire-IO Protocol Specification
Fire-IO is build on a custom networking protocol.
This protocol is designed to handle as much data as possible from many clients, although speed is a priority it is not the primary concern.

# Packet transfer
A single packet goes to a few steps on both sides to complete interaction.
### Packet Sender Steps
 - lock channel
 - frame the packet
 - queue the frames
 - send frame
 - await confirmation
 - send next packet, or unlock when all frames have been send

### Packet Receiver Steps
 - create frame buffer
 - check opcode (check if the frame is first, extra-data, finish, full-data or ping)
 - add to buffer
 - assemble packet from buffer
 - execute
 
 When the sender gets the instruction to send a packet (for example via the API) it locks the channel.
 This means that any other packets will be queued for sending until this interaction succeeded or failed.
 
 If the packet is less than 1KB it is put in to one single frame, with the opcode `SINGLE`, marking that it is a fully contained packet and the receiver does not have to wait for more.
 
 If it is more than 1KB, it sends a frame with the opcode `START` followed by the first 1000 bytes, it repeatedly sends frames with the opcode `CONTINUE` until it has reached the final 1000 bytes, where it sends a frame with the opcode `FINISH` with the remaining content.
 
 Not all frames are send one after the other, after the sender has put out a frame, it waits for the receiver to send a frame back with opcode `CONFIRM_PACKET`, this prevents overflows, miss-aligned or corrupted data and is more efficient than constantly claiming socket time on the main side.
 
 Unknown or corrupted data gets marked by the opcode `UNKNOWN`, registering as a failed packet.
 
 # Meta-Transfer
 The main sends a single-byte-frame to all clients with opcode `PING_PACKET`, when this stream gets interrupted for 5 seconds or more the client registes the connection as DEAD/TIMED-OUT and the connection is closed.
 
 # Opcode table
 Opcodes
 
 | Opcode           | id | hex          |
 |------------------|----|--------------|
 | `SINGLE`         | 1  | `0x00000001` |
 | `START`          | 2  | `0x00000002` |
 | `CONTINUE`       | 3  | `0x00000003` |
 | `FINISH`         | 4  | `0x00000004` |
 | `CONFIRM_PACKET` | 5  | `0x00000005` |
 | `PING_PACKET`    | 6  | `0x00000006` |
 | `UNKNOWN`        | 0  | `0x00000000` |