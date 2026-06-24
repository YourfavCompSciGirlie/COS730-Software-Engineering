import xml.etree.ElementTree as ET

tree = ET.parse('/Users/malaika/School/730/Assignment 1/XMI version.uml')
root = tree.getroot()

XSI = '{http://www.w3.org/2001/XMLSchema-instance}'

for beh in root.iter():
    name = beh.get('name', '')
    if 'Create' in name and 'Research' in name and 'Project' in name:
        msgs = []
        occ_map = {}

        # Build occurrence spec -> lifeline name map
        for child in beh:
            xtype = child.get(XSI + 'type', '')
            if 'Lifeline' in xtype:
                covered = child.get('coveredBy', '')
                ll_name = child.get('name', '')
                for occ_id in covered.split():
                    occ_map[occ_id] = ll_name

        # Collect messages
        for child in beh:
            tag = child.tag.split('}')[-1] if '}' in child.tag else child.tag
            if tag == 'ownedMessage':
                msort = child.get('messageSort', 'call')
                cname = child.get('name', '')
                send_ref = child.get('sendEvent', '')
                recv_ref = child.get('receiveEvent', '')
                msgs.append((cname, msort, send_ref, recv_ref))

        print('=== UC1 Messages ===')
        for cname, msort, send_ref, recv_ref in msgs:
            src = occ_map.get(send_ref, '?')
            dst = occ_map.get(recv_ref, '?')
            print(f'  [{msort:12}] {src:25} --> {dst:25} : {cname}')

        # Also get alt guard text
        print()
        print('=== Combined Fragments (alt guards) ===')
        for child in beh:
            xtype = child.get(XSI + 'type', '')
            if 'CombinedFragment' in xtype:
                print(f'  Fragment: {child.get("name")} operator={child.get("interactionOperator")}')
                for operand in child:
                    otag = operand.tag.split('}')[-1]
                    if 'Operand' in otag or 'operand' in otag:
                        guard = ''
                        for g in operand:
                            gtag = g.tag.split('}')[-1]
                            if 'guard' in gtag or 'Guard' in gtag:
                                for spec in g:
                                    guard = spec.get('value', spec.get('name', ''))
                        print(f'    Operand: {operand.get("name")} guard="{guard}"')
        break
