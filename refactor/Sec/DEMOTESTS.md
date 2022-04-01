# TESTS

Before all tests, do the following:

Start by creating two accounts, using the **open** command, saving both ids. Use the first one as sender and the second as receiver
```shell
open
open
```
### NORMAL RUN TEST

Use the ids created before and confirm that the accounts have been create their balance is for 50.0 (initial balance established), using the **check** command
```shell
check [sender_id]
check [receiver_id]
```
Now, send 25.0 from the sender to the receiver, using the **send** command
```shell
send [sender_id] [receiver_id] 25
```
Confirm that the money has been removed from the sender, using the **check** command
```shell
check [sender_id]
```
Now, use the **check** command to see the Pending movements for the receiver. Save the movement id
```shell
check [receiver_id]
```
Accept the movement, using the **receive** command and the id previously saved. Then, see if the balance has changed
```shell
receive [receiver_id] [movement_id]
check [receiver_id]
```
To see all movements related to an account, use the **audit** command
```shell
audit [receiver_id]
```

### WRONG KEY TEST
Try to audit the account of a wrong id
```shell
audit [wrong_id]
```

### NO MONEY TEST
After creating the accounts, confirm the balance of the sender
```shell
check [sender_id]
```
Try to send to the receiver more money than the sender has in its account
```shell
send [sender_id] [receiver_id] 60
```

### NO MOVEMENT TEST
Start by confirming both accounts exists and have no pending movements
```shell
check [sender_id]
check [receiver_id]
```
Try to accept a movement, using the **receive** command. 
```shell
receive 1000
```

### MOVEMENT ALREADY ACCEPTED TEST
Start by confirming both accounts exists and have no pending movements
```shell
check [sender_id]
check [receiver_id]
```
Now, send 25.0 from the sender to the receiver, using the **send** command
```shell
send [sender_id] [receiver_id] 25
```

Confirm, using the **check** command, that the receiver has a pending movement. Save the movement id
```shell
check [receiver_id]
```
Accept the movement, using the **receive** command and the id previously saved. Then, see if the balance has changed, and if the list of pending movements is empty
```shell
receive [receiver_id] [movement_id]
check [receiver_id]
```
Try to receive the movement again
```shell
receive [receiver_id] [movement_id]
```
